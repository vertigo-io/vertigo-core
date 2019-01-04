/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.mda.domain.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import io.vertigo.studio.masterdata.MasterDataValues;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.domain.ts.model.TSDtDefinitionModel;
import io.vertigo.studio.plugins.mda.domain.ts.model.TSMasterDataDefinitionModel;
import io.vertigo.studio.plugins.mda.util.DomainUtil;
import io.vertigo.util.MapBuilder;

/**
 * Génération des objets relatifs au module Domain.
 *
 * @author rgrange, npiedeloup, pchretien
 */
public final class TSGeneratorPlugin implements GeneratorPlugin {
	private final String targetSubDir;
	private final boolean shouldGenerateDtResourcesTS;
	private final boolean shouldGenerateTsDtDefinitions;
	private final boolean shouldGenerateTsMasterData;
	private final MasterDataManager masterDataManager;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 * @param generateDtResourcesTS Si on génère les fichiers i18n pour les labels des champs en TS
	 * @param generateTsDtDefinitions Si on génère les classes JS.
	 */
	@Inject
	public TSGeneratorPlugin(
			@Named("targetSubDir") final String targetSubDir,
			@Named("generateDtResourcesTS") final boolean generateDtResourcesTS,
			@Named("generateTsDtDefinitions") final boolean generateTsDtDefinitions,
			@Named("generateTsMasterData") final Optional<Boolean> generateTsMasterDataOpt,
			final MasterDataManager masterDataManager) {
		//-----
		this.targetSubDir = targetSubDir;
		shouldGenerateDtResourcesTS = generateDtResourcesTS;
		shouldGenerateTsDtDefinitions = generateTsDtDefinitions;
		shouldGenerateTsMasterData = generateTsMasterDataOpt.orElse(false);
		this.masterDataManager = masterDataManager;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		/* Génération des ressources afférentes au DT mais pour la partie JS.*/
		if (shouldGenerateDtResourcesTS) {
			generateDtResourcesTS(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		}
		/* Génération des fichiers javascripts référençant toutes les définitions. */
		if (shouldGenerateTsDtDefinitions) {
			generateTsDtDefinitions(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		}
		/* Génération des fichiers javascripts référençant toutes les masterdatas. */
		if (shouldGenerateTsMasterData) {
			generateTsMasterData(fileGeneratorConfig, mdaResultBuilder);
		}
	}

	private static List<TSDtDefinitionModel> getTsDtDefinitionModels() {
		return DomainUtil.getDtDefinitions().stream()
				.map(TSDtDefinitionModel::new)
				.collect(Collectors.toList());
	}

	private static void generateTsDtDefinitions(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		for (final TSDtDefinitionModel dtDefinitionModel : getTsDtDefinitionModels()) {
			generateTs(dtDefinitionModel, targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		}
	}

	private void generateTsMasterData(final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {

		final MasterDataValues masterDataValues = masterDataManager.getValues();

		final List<TSMasterDataDefinitionModel> tsMasterDataDefinitionModels = Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.filter(dtDefinition -> dtDefinition.getStereotype() == DtStereotype.StaticMasterData)
				.map(dtDefinition -> new TSMasterDataDefinitionModel(dtDefinition, masterDataValues.getOrDefault(dtDefinition.getClassCanonicalName(), Collections.emptyMap())))
				.collect(Collectors.toList());

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("masterdatas", tsMasterDataDefinitionModels)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName("masterdata.ts")
				.withGenSubDir(targetSubDir)
				.withPackageName("")
				.withTemplateName("domain/ts/template/ts_masterdata.ftl")
				.build()
				.generateFile(mdaResultBuilder);

	}

	private static void generateTs(final TSDtDefinitionModel dtDefinitionModel, final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("classSimpleName", "DtDefinitions")
				.put("dtDefinition", dtDefinitionModel)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(dtDefinitionModel.getJsClassFileName() + ".ts")
				.withGenSubDir(targetSubDir)
				.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".ui." + dtDefinitionModel.getFunctionnalPackageName() + ".config.entity-definition-gen")
				.withTemplateName("domain/ts/template/ts.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	/**
	 * Génère les ressources JS pour les traductions.
	 * @param fileGeneratorConfig Configuration du domaine.
	 */
	private static void generateDtResourcesTS(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {

		final Map<String, List<TSDtDefinitionModel>> packageMap = new HashMap<>();
		for (final TSDtDefinitionModel dtDefinitionModel : getTsDtDefinitionModels()) {
			final String packageName = dtDefinitionModel.getFunctionnalPackageName();
			packageMap.computeIfAbsent(packageName, o -> new ArrayList<>()).add(dtDefinitionModel);
		}

		final String simpleClassName = "DtDefinitions" + "Label";
		//final String packageName = fileGeneratorConfig.getProjectPackageName() + ".domain";

		for (final Entry<String, List<TSDtDefinitionModel>> entry : packageMap.entrySet()) {
			final Map<String, Object> model = new MapBuilder<String, Object>()
					.put("packageName", entry.getKey())
					.put("simpleClassName", simpleClassName)
					.put("dtDefinitions", entry.getValue())
					.build();

			FileGenerator.builder(fileGeneratorConfig)
					.withModel(model)
					.withFileName(entry.getKey() + ".ts")
					.withGenSubDir(targetSubDir)
					.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".ui." + entry.getKey() + ".i18n.generated")
					.withTemplateName("domain/ts/template/propertiesTS.ftl")
					.build()
					.generateFile(mdaResultBuilder);
		}
	}

}
