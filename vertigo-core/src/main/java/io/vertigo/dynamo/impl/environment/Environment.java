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
package io.vertigo.dynamo.impl.environment;

import io.vertigo.core.config.ModuleConfig;
import io.vertigo.core.config.ResourceConfig;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 * Environnement permettant de charger le Modèle.
 * Le Modèle peut être chargé de multiples façon :
 * - par lecture d'un fichier oom (poweramc),
 * - par lecture des annotations java présentes sur les beans,
 * - par lecture de fichiers ksp regoupés dans un projet kpr,
 * - ....
 *  Ces modes de chargement sont extensibles.
 *
 * @author pchretien
 */
public final class Environment {
	private final Map<String, LoaderPlugin> loaderPlugins = new HashMap<>();
	private final List<DynamicRegistryPlugin> dynamicRegistryPlugins;

	/**
	 * Constructeur.
	 */
	Environment(final List<DynamicRegistryPlugin> dynamicRegistryPlugins, final List<LoaderPlugin> loaderPlugins) {
		Assertion.checkNotNull(dynamicRegistryPlugins);
		Assertion.checkNotNull(loaderPlugins);
		//-----
		this.dynamicRegistryPlugins = dynamicRegistryPlugins;
		//On enregistre les loaders
		for (final LoaderPlugin loaderPlugin : loaderPlugins) {
			this.loaderPlugins.put(loaderPlugin.getType(), loaderPlugin);
		}
	}

	/**
	 * @param resourceConfigs List of resources (must be in a type managed by this loader)
	 */
	private void parse(final List<ResourceConfig> resourceConfigs) {
		final CompositeDynamicRegistry handler = new CompositeDynamicRegistry(dynamicRegistryPlugins);

		//Création du repositoy des instances le la grammaire (=> model)
		final DynamicDefinitionRepository dynamicModelRepository = new DynamicDefinitionRepository(handler);

		//--Enregistrement des types primitifs
		for (final DynamicRegistryPlugin dynamicRegistryPlugin : dynamicRegistryPlugins) {
			for (final DynamicDefinition dynamicDefinition : dynamicRegistryPlugin.getRootDynamicDefinitions()) {
				dynamicModelRepository.addDefinition(dynamicDefinition);
			}
		}
		for (final ResourceConfig resourceConfig : resourceConfigs) {
			final LoaderPlugin loaderPlugin = loaderPlugins.get(resourceConfig.getType());
			Assertion.checkNotNull(loaderPlugin, "This resource {0} can not be parse by this loader", resourceConfig);
			loaderPlugin.load(resourceConfig.getPath(), dynamicModelRepository);
		}
		dynamicModelRepository.solve();
	}

	//	/**
	//	 * @return Types that can be parsed.
	//	 */
	//	public Set<String> getTypes() {
	//		return Collections.unmodifiableSet(loaderPlugins.keySet());
	//	}

	public void injectDefinitions(final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		for (final ModuleConfig moduleConfig : moduleConfigs) {
			injectDefinitions(moduleConfig);
		}
	}

	private void injectDefinitions(final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//-----
		final List<ResourceConfig> resourceConfigs = moduleConfig.getResourceConfigs();
		if (!resourceConfigs.isEmpty()) {
			this.parse(resourceConfigs);
		}
	}

}
