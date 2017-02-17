/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.DefinitionResourceConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.definition.dsl.dynamic.DslDefinition;
import io.vertigo.core.definition.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.util.ClassUtil;

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
	private Stream<Definition> parse(final DefinitionSpace definitionSpace, final List<DefinitionResourceConfig> definitionResourceConfigs) {
		final CompositeDynamicRegistry dynamicRegistry = new CompositeDynamicRegistry(dynamicRegistryPlugins);

		//Création du repositoy des instances le la grammaire (=> model)
		final DslDefinitionRepository dynamicModelRepository = new DslDefinitionRepository(dynamicRegistry);

		//--Enregistrement des types primitifs
		for (final DynamicRegistryPlugin dynamicRegistryPlugin : dynamicRegistryPlugins) {
			for (final DslDefinition dslDefinition : dynamicRegistryPlugin.getGrammar().getRootDefinitions()) {
				dynamicModelRepository.addDefinition(dslDefinition);
			}
		}
		for (final DefinitionResourceConfig definitionResourceConfig : definitionResourceConfigs) {
			final LoaderPlugin loaderPlugin = loaderPlugins.get(definitionResourceConfig.getType());
			Assertion.checkNotNull(loaderPlugin, "This resource {0} can not be parse by these loaders : {1}", definitionResourceConfig, loaderPlugins.keySet());
			loaderPlugin.load(definitionResourceConfig.getPath(), dynamicModelRepository);
		}

		return dynamicModelRepository.solve(definitionSpace);
	}

	public void injectDefinitions(final DefinitionSpace definitionSpace, final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		moduleConfigs
				.stream()
				.flatMap(moduleConfig -> createDefinitions(definitionSpace, moduleConfig))
				.forEach(definitionSpace::put); //Here all definitions are registered into the definitionSpace
	}

	private Stream<Definition> createDefinitions(final DefinitionSpace definitionSpace, final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//-----
		return Stream.concat(
				parse(definitionSpace, moduleConfig.getDefinitionResourceConfigs()), //case by Resource
				provide(moduleConfig.getDefinitionProviderConfigs()));
	}

	private Stream<Definition> provide(final List<DefinitionProviderConfig> definitionProviderConfigs) {
		return definitionProviderConfigs
				.stream()
				.map(definitionProviderConfig -> ClassUtil.newInstance(definitionProviderConfig.getDefinitionProviderClass()))
				.flatMap(definitionProvider -> definitionProvider.get().stream());

	}
}
