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
package io.vertigo.studio.plugins.mda.domain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.domain.model.DtDefinitionModel;
import io.vertigo.util.MapBuilder;

/**
 * Génération des objets relatifs au module Domain.
 *
 * @author pchretien
 */
public final class JSGeneratorPlugin implements GeneratorPlugin {
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
	public JSGeneratorPlugin(
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

	private static void generateJsDtDefinitions(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		final List<DtDefinitionModel> dtDefinitions = DomainUtil.getDtDefinitions()
				.stream()
				.map(DtDefinitionModel::new)
				.collect(Collectors.toList());

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("packageName", fileGeneratorConfig.getProjectPackageName() + ".domain")
				.put("classSimpleName", "DtDefinitions")
				.put("dtDefinitions", dtDefinitions)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName("DtDefinitions.js")
				.withGenSubDir(targetSubDir)
				.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".domain")
				.withTemplateName("domain/template/js.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	/**
	 * Génère les ressources JS pour les traductions.
	 * @param fileGeneratorConfig Configuration du domaine.
	 */
	private static void generateDtResourcesJS(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		final List<DtDefinitionModel> dtDefinitions = DomainUtil.getDtDefinitions()
				.stream()
				.map(DtDefinitionModel::new)
				.collect(Collectors.toList());

		final String simpleClassName = "DtDefinitions" + "Label";
		final String packageName = fileGeneratorConfig.getProjectPackageName() + ".domain";

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("packageName", packageName)
				.put("simpleClassName", simpleClassName)
				.put("dtDefinitions", dtDefinitions)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(simpleClassName + ".js")
				.withGenSubDir(targetSubDir)
				.withPackageName(packageName)
				.withTemplateName("domain/template/propertiesJS.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

}
