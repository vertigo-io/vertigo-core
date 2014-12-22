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
package io.vertigo.studio.impl.mda;

import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaManager;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.mda.ResultBuilder;
import io.vertigo.studio.plugins.mda.FileConfiguration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation du MDA.
 *
 * @author pchretien, dchallas
 */
public final class MdaManagerImpl implements MdaManager {
	private final List<GeneratorPlugin> generatorPlugins;
	private final FileConfiguration fileConfiguration;

	@Inject
	/**
	 *
	 * @param generatorPlugins
	 * @param targetGenDir Répertoire des fichiers TOUJOURS générés
	 * @param projectPackageName Racine du projet.
	 * @param encoding Encoding des fichiers générés.
	 */
	public MdaManagerImpl(final List<GeneratorPlugin> generatorPlugins,
			@Named("targetGenDir") final String targetGenDir,
			@Named("projectPackageName") final String projectPackageName,
			@Named("encoding") final String encoding) {
		Assertion.checkNotNull(generatorPlugins);
		Assertion.checkArgNotEmpty(targetGenDir);
		Assertion.checkArgNotEmpty(projectPackageName);
		Assertion.checkArgNotEmpty(encoding);
		//-----
		this.generatorPlugins = java.util.Collections.unmodifiableList(generatorPlugins);
		fileConfiguration = new FileConfiguration(targetGenDir, projectPackageName, encoding);
	}

	/** {@inheritDoc} */
	@Override
	public Result generate() {
		//Création d'un objet listant les résultats
		final ResultBuilder resultBuilder = new ResultBuilder();
		//Génèration des objets issus de la modélisation
		for (final GeneratorPlugin generatorPlugin : generatorPlugins) {
			generatorPlugin.generate(fileConfiguration, resultBuilder);
		}
		return resultBuilder.build();
	}
}
