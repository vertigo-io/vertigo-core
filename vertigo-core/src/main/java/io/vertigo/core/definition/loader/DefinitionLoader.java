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
package io.vertigo.core.definition.loader;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.component.ComponentSpace;
import io.vertigo.core.component.loader.ComponentLoader;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionProvider;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;

/**
 * A DefinitionLoader uses all the DefinitionProviders of all the modules to register all definitions at once at the beginning.
 * Use DynamoDefinitionProvider to use the DSL.
 *
 * @author pchretien
 */
public final class DefinitionLoader {
	private final DefinitionSpace definitionSpace;
	private final ComponentSpace componentSpace;

	/**
	 * Loader of definitions
	 * @param definitionSpace the definitionSpace to build
	 * @param componentSpace the componentSpace
	 */
	public DefinitionLoader(final DefinitionSpace definitionSpace, final ComponentSpace componentSpace) {
		Assertion.checkNotNull(definitionSpace);
		Assertion.checkNotNull(componentSpace);
		//-----
		this.definitionSpace = definitionSpace;
		this.componentSpace = componentSpace;
	}

	/**
	 * Inject all the definition of the modules.
	 *
	 * @param moduleConfigs module configs
	 * @return a stream of definitions
	 */
	public Stream<Definition> createDefinitions(final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		return moduleConfigs
				.stream()
				.flatMap(moduleConfig -> provide(moduleConfig.getDefinitionProviderConfigs()))
				.map(supplier -> supplier.get(definitionSpace));
	}

	public Stream<Definition> createDefinitionsFromComponents() {
		return componentSpace.keySet()
				.stream()
				.map(key -> componentSpace.resolve(key, Component.class))
				.filter(component -> DefinitionProvider.class.isAssignableFrom(component.getClass()))
				.flatMap(component -> ((DefinitionProvider) component).get(definitionSpace).stream())
				.map(defitionSupplier -> defitionSupplier.get(definitionSpace));
	}

	private Stream<DefinitionSupplier> provide(final List<DefinitionProviderConfig> definitionProviderConfigs) {
		return definitionProviderConfigs
				.stream()
				.map(this::createDefinitionProvider)
				.flatMap(definitionProvider -> definitionProvider.get(definitionSpace).stream());
	}

	private DefinitionProvider createDefinitionProvider(final DefinitionProviderConfig definitionProviderConfig) {
		final DefinitionProvider definitionProvider = ComponentLoader.createInstance(definitionProviderConfig.getDefinitionProviderClass(), componentSpace, Optional.empty(),
				definitionProviderConfig.getParams());

		definitionProviderConfig.getDefinitionResourceConfigs()
				.forEach(definitionProvider::addDefinitionResourceConfig);

		return definitionProvider;

	}
}
