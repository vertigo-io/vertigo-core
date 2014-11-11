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
import io.vertigo.studio.mda.Configuration;
import io.vertigo.studio.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaManager;
import io.vertigo.studio.mda.Result;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

/**
 * Implémentation du MDA.
 * 
 * @author pchretien, dchallas
 */
public final class MdaManagerImpl implements MdaManager {
	private final List<GeneratorPlugin<Configuration>> generatorPlugins;

	@Inject
	public MdaManagerImpl(final List<GeneratorPlugin<Configuration>> generatorPlugins) {
		Assertion.checkNotNull(generatorPlugins);
		//---------------------------------------------------------------------
		this.generatorPlugins = java.util.Collections.unmodifiableList(generatorPlugins);
	}

	/** {@inheritDoc} */
	@Override
	public Result generate(final Properties properties) {
		//Création d'un objet listant les résultats
		final Result result = new ResultImpl();
		//Génèration des objets issus de la modélisation
		for (final GeneratorPlugin<Configuration> generatorPlugin : generatorPlugins) {
			final Configuration c = generatorPlugin.createConfiguration(properties);
			generatorPlugin.generate(c, result);
		}
		return result;
	}
}
