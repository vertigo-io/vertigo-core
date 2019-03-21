/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.studio.plugins.mda.domain.java;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.masterdata.MasterDataManager;
import io.vertigo.studio.masterdata.MasterDataValue;
import io.vertigo.studio.masterdata.MasterDataValues;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.domain.java.model.DtDefinitionModel;
import io.vertigo.studio.plugins.mda.domain.java.model.MethodAnnotationsModel;
import io.vertigo.studio.plugins.mda.domain.java.model.masterdata.MasterDataDefinitionModel;
import io.vertigo.studio.plugins.mda.util.DomainUtil;
import io.vertigo.util.MapBuilder;

/**
 * Génération des objets relatifs au module Domain.
 *
 * @author pchretien
 */
public final class DomainGeneratorPlugin implements GeneratorPlugin {
	private final String targetSubDir;
	private final boolean shouldGenerateDtResources;
	private final boolean shouldGenerateJpaAnnotations;
	private final boolean shouldGenerateDtDefinitions;
	private final boolean shouldGenerateDtObject;
	private final String dictionaryClassName;

	private final Optional<MasterDataManager> masterDataManagerOpt;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 * @param generateDtResources Si on génère les fichiers i18n pour MessageText des labels des champs
	 * @param generateJpaAnnotations Si on ajoute les annotations JPA
	 * @param generateDtDefinitions Si on génère le fichier fournissant la liste des classes de Dt
	 * @param generateDtObject Si on génère les classes des Dt
	 */
	@Inject
	public DomainGeneratorPlugin(
			@Named("targetSubDir") final String targetSubDir,
			@Named("generateDtResources") final boolean generateDtResources,
			@Named("generateJpaAnnotations") final boolean generateJpaAnnotations,
			@Named("generateDtDefinitions") final boolean generateDtDefinitions,
			@Named("dictionaryClassName") final Optional<String> dictionaryClassNameOption,
			@Named("generateDtObject") final boolean generateDtObject,
			final Optional<MasterDataManager> masterDataManagerOpt) {
		//-----
		this.targetSubDir = targetSubDir;
		shouldGenerateDtResources = generateDtResources;
		shouldGenerateJpaAnnotations = generateJpaAnnotations;
		shouldGenerateDtDefinitions = generateDtDefinitions;
		dictionaryClassName = dictionaryClassNameOption.orElse("DtDefinitions");
		shouldGenerateDtObject = generateDtObject;
		//---
		this.masterDataManagerOpt = masterDataManagerOpt;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		/* Génération des ressources afférentes au DT. */
		if (shouldGenerateDtResources) {
			generateDtResources(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		}

		/* Génération de la lgeneratee référençant toutes des définitions. */
		if (shouldGenerateDtDefinitions) {
			generateDtDefinitions(targetSubDir, fileGeneratorConfig, mdaResultBuilder, dictionaryClassName);
		}

		/* Générations des DTO. */
		if (shouldGenerateDtObject) {
			generateDtObjects(fileGeneratorConfig, mdaResultBuilder);
			generateJavaEnums(fileGeneratorConfig, mdaResultBuilder);
		}

	}

	private static void generateDtDefinitions(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final String dictionaryClassName) {

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("packageName", fileGeneratorConfig.getProjectPackageName() + ".domain")
				.put("classSimpleName", dictionaryClassName)
				.put("dtDefinitions", toModels(DomainUtil.getDtDefinitions()))
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(dictionaryClassName + ".java")
				.withGenSubDir(targetSubDir)
				.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".domain")
				.withTemplateName("domain/java/template/dtdefinitions.ftl")
				.build()
				.generateFile(mdaResultBuilder);

	}

	private void generateDtObjects(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		for (final DtDefinition dtDefinition : DomainUtil.getDtDefinitions()) {
			generateDtObject(fileGeneratorConfig, mdaResultBuilder, dtDefinition);
		}
	}

	private void generateDtObject(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final DtDefinition dtDefinition) {
		final DtDefinitionModel dtDefinitionModel = new DtDefinitionModel(dtDefinition);

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("dtDefinition", dtDefinitionModel)
				.put("annotations", new MethodAnnotationsModel(shouldGenerateJpaAnnotations))
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(dtDefinitionModel.getClassSimpleName() + ".java")
				.withGenSubDir(targetSubDir)
				.withPackageName(dtDefinitionModel.getPackageName())
				.withTemplateName("domain/java/template/dto.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	private static List<DtDefinitionModel> toModels(final Collection<DtDefinition> dtDefinitions) {
		return dtDefinitions.stream()
				.map(DtDefinitionModel::new)
				.collect(Collectors.toList());
	}

	private static void generateDtResources(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		final String simpleClassName = "DtResources";
		final String resourcesTemplateName = "domain/java/template/resources.ftl";
		final String propertiesTemplateName = "domain/java/template/properties.ftl";

		//pour les .properties on force l'ISO-8859-1 comme la norme l'impose
		final FileGeneratorConfig propertiesFileConfig = new FileGeneratorConfig(fileGeneratorConfig.getTargetGenDir(), fileGeneratorConfig.getProjectPackageName(), "ISO-8859-1");

		/**
		 * Génération des ressources afférentes au DT.
		 */
		for (final Entry<String, Collection<DtDefinition>> entry : DomainUtil.getDtDefinitionCollectionMap().entrySet()) {
			final Collection<DtDefinition> dtDefinitions = entry.getValue();
			Assertion.checkNotNull(dtDefinitions);
			final String packageName = entry.getKey();

			final Map<String, Object> model = new MapBuilder<String, Object>()
					.put("packageName", packageName)
					.put("simpleClassName", simpleClassName)
					.put("dtDefinitions", toModels(dtDefinitions))
					.build();

			FileGenerator.builder(fileGeneratorConfig)
					.withModel(model)
					.withFileName(simpleClassName + ".java")
					.withGenSubDir(targetSubDir)
					.withPackageName(packageName)
					.withTemplateName(resourcesTemplateName)
					.build()
					.generateFile(mdaResultBuilder);

			FileGenerator.builder(propertiesFileConfig)
					.withModel(model)
					.withFileName(simpleClassName + ".properties")
					.withGenSubDir(targetSubDir)
					.withPackageName(packageName)
					.withTemplateName(propertiesTemplateName)
					.build()
					.generateFile(mdaResultBuilder);
		}
	}

	private void generateJavaEnums(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		final MasterDataValues masterDataValues = masterDataManagerOpt.isPresent() ? masterDataManagerOpt.get().getValues() : new MasterDataValues();

		Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.filter(dtDefinition -> dtDefinition.getStereotype() == DtStereotype.StaticMasterData)
				.forEach(dtDefintion -> generateJavaEnum(
						fileGeneratorConfig,
						mdaResultBuilder,
						dtDefintion, masterDataValues.getOrDefault(dtDefintion.getClassCanonicalName(), Collections.emptyMap())));
	}

	private void generateJavaEnum(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final DtDefinition dtDefinition,
			final Map<String, MasterDataValue> values) {

		final MasterDataDefinitionModel masterDataDefinitionModel = new MasterDataDefinitionModel(dtDefinition, values);

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("entity", masterDataDefinitionModel)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(masterDataDefinitionModel.getClassSimpleName() + "Enum.java")
				.withGenSubDir(targetSubDir)
				.withPackageName(dtDefinition.getPackageName())
				.withTemplateName("domain/java/template/masterdata_enum.ftl")
				.build()
				.generateFile(mdaResultBuilder);

	}
}
