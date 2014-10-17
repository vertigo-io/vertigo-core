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

import io.vertigo.core.di.configurator.ResourceConfig;
import io.vertigo.core.spaces.resource.ResourceLoader;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.lang.Assertion;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
final class Environment implements ResourceLoader {
	private final Map<String, Loader> loaders = new HashMap<>();
	private final List<DynamicRegistryPlugin> dynamicRegistryPlugins;

	/**
	 * Constructeur.
	 * @param dynamicModelRepository  DynamicModelRepository
	 */
	Environment(final List<DynamicRegistryPlugin> dynamicRegistryPlugins, final List<LoaderPlugin> loaderPlugins) {
		Assertion.checkNotNull(dynamicRegistryPlugins);
		Assertion.checkNotNull(loaderPlugins);
		//---------------------------------------------------------------------
		this.dynamicRegistryPlugins = dynamicRegistryPlugins;
		//On enregistre les loaders
		for (final LoaderPlugin loaderPlugin : loaderPlugins) {
			loaders.put(loaderPlugin.getType(), loaderPlugin);
		}
	}

	public void parse(final List<ResourceConfig> resourceConfigs) {
		final CompositeDynamicRegistry handler = new CompositeDynamicRegistry(dynamicRegistryPlugins);

		//Création du repositoy des instances le la grammaire (=> model)
		final DynamicDefinitionRepository dynamicModelRepository = new DynamicDefinitionRepository(handler);

		//--Enregistrement des types primitifs
		final Entity dataTypeEntity = KernelGrammar.getDataTypeEntity();
		for (final DataType type : DataType.values()) {
			final DynamicDefinition definition = DynamicDefinitionRepository.createDynamicDefinitionBuilder(type.name(), dataTypeEntity, null).build();
			dynamicModelRepository.addDefinition(definition);
		}
		for (final ResourceConfig resourceConfig : resourceConfigs) {
			final Loader loader = loaders.get(resourceConfig.getType());
			Assertion.checkNotNull(loader, "This resource {0} can not be parse by this loader", resourceConfig);
			loader.load(resourceConfig.getPath(), dynamicModelRepository);
		}
		dynamicModelRepository.solve();
	}

	public Set<String> getTypes() {
		return Collections.unmodifiableSet(loaders.keySet());
	}
}
