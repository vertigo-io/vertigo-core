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
import io.vertigo.util.MapBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Génération des objets relatifs au module Domain.
 *
 * @author pchretien
 */
public final class JSGeneratorPlugin extends AbstractGeneratorPlugin {
	private final String targetSubDir;
	private final boolean generateDtResourcesJS;
	private final boolean generateJsDtDefinitions;

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
		this.generateDtResourcesJS = generateDtResourcesJS;
		this.generateJsDtDefinitions = generateJsDtDefinitions;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileConfig domainConfig, final ResultBuilder resultBuilder) {
		Assertion.checkNotNull(domainConfig);
		Assertion.checkNotNull(resultBuilder);
		//-----
		/* Génération des ressources afférentes au DT mais pour la partie JS.*/
		if (generateDtResourcesJS) {
			generateDtResourcesJS(targetSubDir, domainConfig, resultBuilder);
		}
		/* Génération des fichiers javascripts référençant toutes les définitions. */
		if (generateJsDtDefinitions) {
			generateJsDtDefinitions(targetSubDir, domainConfig, resultBuilder);
		}
	}

	private static void generateJsDtDefinitions(final String targetSubDir, final FileConfig domainConfiguration, final ResultBuilder resultBuilder) {

		final List<TemplateDtDefinition> dtDefinitions = new ArrayList<>();
		for (final DtDefinition dtDefinition : DomainUtil.getDtDefinitions()) {
			dtDefinitions.add(new TemplateDtDefinition(dtDefinition));
		}

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("packageName", domainConfiguration.getProjectPackageName() + ".domain")
				.put("classSimpleName", "DtDefinitions")
				.put("dtDefinitions", dtDefinitions)
				.build();

		createFileGenerator(domainConfiguration, mapRoot, "DtDefinitions", targetSubDir, domainConfiguration.getProjectPackageName() + ".domain", ".js", "domain/templates/js.ftl")
				.generateFile(resultBuilder);

	}

	/**
	 * Génère les ressources JS pour les traductions.
	 * @param domainConfiguration Configuration du domaine.
	 */
	private static void generateDtResourcesJS(final String targetSubDir, final FileConfig domainConfiguration, final ResultBuilder result) {
		final List<TemplateDtDefinition> dtDefinitions = new ArrayList<>();
		for (final DtDefinition dtDefinition : DomainUtil.getDtDefinitions()) {
			dtDefinitions.add(new TemplateDtDefinition(dtDefinition));
		}

		final String simpleClassName = "DtDefinitions" + "Label";
		final String packageName = domainConfiguration.getProjectPackageName() + ".domain";

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("packageName", packageName)
				.put("simpleClassName", simpleClassName)
				.put("dtDefinitions", dtDefinitions)
				.build();

		createFileGenerator(domainConfiguration, mapRoot, simpleClassName, targetSubDir, packageName, ".js", "domain/templates/propertiesJS.ftl").generateFile(result);
	}

}
