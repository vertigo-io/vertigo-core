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
package io.vertigo.core.definition.loader;

import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.DefinitionResourceConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinitionRepository;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.util.ClassUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
public final class DefinitionLoader implements Component {
	private final Map<String, LoaderPlugin> loaderPlugins;
	private final List<DynamicRegistryPlugin> dynamicRegistryPlugins;

	/**
	 * Constructeur.
	 */
	@Inject
	public DefinitionLoader(final List<DynamicRegistryPlugin> dynamicRegistryPlugins, final List<LoaderPlugin> loaderPlugins) {
		Assertion.checkNotNull(dynamicRegistryPlugins);
		Assertion.checkNotNull(loaderPlugins);
		//-----
		this.dynamicRegistryPlugins = dynamicRegistryPlugins;
		//On enregistre les loaders
		this.loaderPlugins = new HashMap<>();
		for (final LoaderPlugin loaderPlugin : loaderPlugins) {
			this.loaderPlugins.put(loaderPlugin.getType(), loaderPlugin);
		}
	}

	/**
	 * @param definitionResourceConfigs List of resources (must be in a type managed by this loader)
	 */
	private void parse(final DefinitionSpace definitionSpace, final List<DefinitionResourceConfig> definitionResourceConfigs) {
		final CompositeDynamicRegistry handler = new CompositeDynamicRegistry(dynamicRegistryPlugins);

		//Création du repositoy des instances le la grammaire (=> model)
		final DynamicDefinitionRepository dynamicModelRepository = new DynamicDefinitionRepository(handler);

		//--Enregistrement des types primitifs
		for (final DynamicRegistryPlugin dynamicRegistryPlugin : dynamicRegistryPlugins) {
			for (final DynamicDefinition dynamicDefinition : dynamicRegistryPlugin.getRootDynamicDefinitions()) {
				dynamicModelRepository.addDefinition(dynamicDefinition);
			}
		}
		for (final DefinitionResourceConfig definitionResourceConfig : definitionResourceConfigs) {
			final LoaderPlugin loaderPlugin = loaderPlugins.get(definitionResourceConfig.getType());
			Assertion.checkNotNull(loaderPlugin, "This resource {0} can not be parse by these loaders : {1}", definitionResourceConfig, loaderPlugins.keySet());
			loaderPlugin.load(definitionResourceConfig.getPath(), dynamicModelRepository);
		}
		dynamicModelRepository.solve(definitionSpace);
	}

	public void injectDefinitions(final DefinitionSpace definitionSpace, final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		for (final ModuleConfig moduleConfig : moduleConfigs) {
			injectDefinitions(definitionSpace, moduleConfig);
		}
	}

	private void injectDefinitions(final DefinitionSpace definitionSpace, final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//-----
		final List<DefinitionResourceConfig> definitionResourceConfigs = moduleConfig.getDefinitionResourceConfigs();
		if (!definitionResourceConfigs.isEmpty()) {
			this.parse(definitionSpace, definitionResourceConfigs);
		}
		//-----
		for (final DefinitionProviderConfig definitionProviderConfig : moduleConfig.getDefinitionProviderConfigs()) {
			final DefinitionProvider definitionProvider = ClassUtil.newInstance(definitionProviderConfig.getDefinitionProviderClass());
			for (final Definition definition : definitionProvider) {
				definitionSpace.put(definition);
			}
		}
	}
}
