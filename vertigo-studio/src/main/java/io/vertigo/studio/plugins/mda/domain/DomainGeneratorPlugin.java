/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.ResultBuilder;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.FileConfig;
import io.vertigo.studio.plugins.mda.domain.templates.TemplateDtDefinition;
import io.vertigo.studio.plugins.mda.domain.templates.TemplateMethodAnnotations;
import io.vertigo.util.MapBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Génération des objets relatifs au module Domain.
 *
 * @author pchretien
 */
public final class DomainGeneratorPlugin extends AbstractGeneratorPlugin {
	private final String targetSubDir;
	private final boolean generateDtResources;
	private final boolean generateJpaAnnotations;
	private final boolean generateDtDefinitions;
	private final boolean generateDtObject;

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
			@Named("generateDtObject") final boolean generateDtObject) {
		//-----
		this.targetSubDir = targetSubDir;
		this.generateDtResources = generateDtResources;
		this.generateJpaAnnotations = generateJpaAnnotations;
		this.generateDtDefinitions = generateDtDefinitions;
		this.generateDtObject = generateDtObject;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileConfig fileConfig, final ResultBuilder resultBuilder) {
		Assertion.checkNotNull(fileConfig);
		Assertion.checkNotNull(resultBuilder);
		//-----
		/* Génération des ressources afférentes au DT. */
		if (generateDtResources) {
			generateDtResources(targetSubDir, fileConfig, resultBuilder);
		}

		/* Génération de la lgeneratee référençant toutes des définitions. */
		if (generateDtDefinitions) {
			generateDtDefinitions(targetSubDir, fileConfig, resultBuilder);
		}

		/* Générations des DTO. */
		if (generateDtObject) {
			generateDtObjects(fileConfig, resultBuilder);
		}

	}

	private static void generateDtDefinitions(final String targetSubDir, final FileConfig fileConfig, final ResultBuilder resultBuilder) {

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("packageName", fileConfig.getProjectPackageName() + ".domain")
				.put("classSimpleName", "DtDefinitions")
				.put("dtDefinitions", DomainUtil.getDtDefinitions())
				.build();

		createFileGenerator(fileConfig, mapRoot, "DtDefinitions", targetSubDir, fileConfig.getProjectPackageName() + ".domain", ".java", "domain/templates/dtdefinitions.ftl")
				.generateFile(resultBuilder);

	}

	private void generateDtObjects(final FileConfig fileConfig, final ResultBuilder resultBuilder) {
		for (final DtDefinition dtDefinition : DomainUtil.getDtDefinitions()) {
			generateDtObject(fileConfig, resultBuilder, dtDefinition);
		}
	}

	private void generateDtObject(final FileConfig fileConfig, final ResultBuilder resultBuilder, final DtDefinition dtDefinition) {
		final TemplateDtDefinition definition = new TemplateDtDefinition(dtDefinition);

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("dtDefinition", definition)
				.put("annotations", new TemplateMethodAnnotations(generateJpaAnnotations))
				.build();

		createFileGenerator(fileConfig, mapRoot, definition.getClassSimpleName(), targetSubDir, definition.getPackageName(), ".java", "domain/templates/dto.ftl")
				.generateFile(resultBuilder);
	}

	private static void generateDtResources(final String targetSubDir, final FileConfig fileConfig, final ResultBuilder resultBuilder) {
		final String simpleClassName = "DtResources";
		/**
		 * Génération des ressources afférentes au DT.
		 */
		for (final Entry<String, Collection<DtDefinition>> entry : DomainUtil.getDtDefinitionCollectionMap().entrySet()) {
			final Collection<DtDefinition> dtDefinitionCollection = entry.getValue();
			Assertion.checkNotNull(dtDefinitionCollection);
			final String packageName = entry.getKey();

			final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
					.put("packageName", packageName)
					.put("simpleClassName", simpleClassName)
					.put("dtDefinitions", dtDefinitionCollection)
					.build();

			createFileGenerator(fileConfig, mapRoot, simpleClassName, targetSubDir, packageName, ".java", "domain/templates/resources.ftl")
					.generateFile(resultBuilder);

			createFileGenerator(fileConfig, mapRoot, simpleClassName, targetSubDir, packageName, ".properties", "domain/templates/properties.ftl")
					.generateFile(resultBuilder);
		}
	}
}
