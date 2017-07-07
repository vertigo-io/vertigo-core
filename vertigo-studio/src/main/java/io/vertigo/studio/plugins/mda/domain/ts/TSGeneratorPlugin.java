/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.domain.ts.model.JSDtDefinitionModel;
import io.vertigo.studio.plugins.mda.util.DomainUtil;
import io.vertigo.util.MapBuilder;

/**
 * Génération des objets relatifs au module Domain.
 *
 * @author rgrange, npiedeloup, pchretien
 */
public final class TSGeneratorPlugin implements GeneratorPlugin {
	private final String targetSubDir;
	private final boolean shouldGenerateDtResourcesJS;
	private final boolean shouldGenerateJsDtDefinitions;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 * @param generateDtResourcesJS Si on génère les fichiers i18n pour les labels des champs en JS
	 * @param generateJsDtDefinitions Si on génère les classes JS.
	 */
	@Inject
	public TSGeneratorPlugin(
			@Named("targetSubDir") final String targetSubDir,
			@Named("generateDtResourcesJS") final boolean generateDtResourcesJS,
			@Named("generateJsDtDefinitions") final boolean generateJsDtDefinitions) {
		//-----
		this.targetSubDir = targetSubDir;
		shouldGenerateDtResourcesJS = generateDtResourcesJS;
		shouldGenerateJsDtDefinitions = generateJsDtDefinitions;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		/* Génération des ressources afférentes au DT mais pour la partie JS.*/
		if (shouldGenerateDtResourcesJS) {
			generateDtResourcesJS(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		}
		/* Génération des fichiers javascripts référençant toutes les définitions. */
		if (shouldGenerateJsDtDefinitions) {
			generateJsDtDefinitions(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		}
	}

	private static List<JSDtDefinitionModel> getJsDtDefinitionModels() {
		return DomainUtil.getDtDefinitions().stream()
				.map(JSDtDefinitionModel::new)
				.collect(Collectors.toList());
	}

	private static void generateJsDtDefinitions(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		for (final JSDtDefinitionModel dtDefinitionModel : getJsDtDefinitionModels()) {
			generateJs(dtDefinitionModel, targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		}
	}

	private static void generateJs(final JSDtDefinitionModel dtDefinitionModel, final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("classSimpleName", "DtDefinitions")
				.put("dtDefinition", dtDefinitionModel)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(dtDefinitionModel.getJsClassFileName() + ".ts")
				.withGenSubDir(targetSubDir)
				.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".ui." + dtDefinitionModel.getFunctionnalPackageName() + ".config.entity-definition-gen")
				.withTemplateName("domain/ts/template/js.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	/**
	 * Génère les ressources JS pour les traductions.
	 * @param fileGeneratorConfig Configuration du domaine.
	 */
	private static void generateDtResourcesJS(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {

		final Map<String, List<JSDtDefinitionModel>> packageMap = new HashMap<>();
		for (final JSDtDefinitionModel dtDefinitionModel : getJsDtDefinitionModels()) {
			final String packageName = dtDefinitionModel.getFunctionnalPackageName();
			packageMap.computeIfAbsent(packageName, o -> new ArrayList<>()).add(dtDefinitionModel);
		}

		final String simpleClassName = "DtDefinitions" + "Label";
		//final String packageName = fileGeneratorConfig.getProjectPackageName() + ".domain";

		for (final Entry<String, List<JSDtDefinitionModel>> entry : packageMap.entrySet()) {
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
					.withTemplateName("domain/ts/template/propertiesJS.ftl")
					.build()
					.generateFile(mdaResultBuilder);
		}
	}

}
