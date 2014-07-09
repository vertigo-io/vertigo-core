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

import io.vertigo.kernel.lang.Activeable;
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
public final class MdaManagerImpl implements MdaManager, Activeable {
	@Inject
	private List<GeneratorPlugin<Configuration>> generatorPlugins;

	/** {@inheritDoc} */
	public void start() {
		//Pour des raisons d'optimisation mémoire on ne garde pas la liste des plugins dans le manager.
		//Car ceux ci ne sont utilisé qu'une seule fois.
		//En revanche on test au démarrage la présence des class des plugins. (et on ne s'occupe pas du resultat)
		//getGeneratorPlugins();
	}

	/** {@inheritDoc} */
	public void stop() {
		//
	}

	/** {@inheritDoc} */
	public List<GeneratorPlugin<Configuration>> getGeneratorPlugins() {
		//		final List<GeneratorPlugin<Configuration>> generatorPlugins = new ArrayList<GeneratorPlugin<Configuration>>();
		//		for (final Plugin plugin : Home.getContainer().getPlugins(MdaManager.class)) {
		//			Assertion.precondition(plugin instanceof GeneratorPlugin, "Les plugins doivent tous être des GeneratorPlugin ({0})", plugin.getClass().getName());
		//			generatorPlugins.add((GeneratorPlugin<Configuration>) plugin);
		//		}
		return java.util.Collections.unmodifiableList(generatorPlugins);
	}

	/** {@inheritDoc} */
	public Result generate(final Properties properties) {
		//Création d'un objet listant les résultats
		final Result result = new ResultImpl();
		//Génèration des objets issus de la modélisation
		for (final GeneratorPlugin<Configuration> generatorPlugin : getGeneratorPlugins()) {
			final Configuration c = generatorPlugin.createConfiguration(properties);
			generatorPlugin.generate(c, result);
		}
		return result;
	}
}
