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

/**

 * A DefinitionLoader uses all the DefinitionProviders of all the modules to register all definitions at once at the beginning.
 * Use DynamoDefinitionProvider to use the DSL.
 *
 * @author pchretien
 */
public final class DefinitionLoader {

	/**
	 * Inject all the definition of the modules.
	 * @param definitionSpace the definitionSpace to build
	 * @param componentSpace the componentSpace
	 * @param moduleConfigs module configs
	 */
	public static Stream<Definition> createDefinitions(final DefinitionSpace definitionSpace, final ComponentSpace componentSpace, final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		return moduleConfigs
				.stream()
				.peek(Assertion::checkNotNull)
				.flatMap(moduleConfig -> provide(definitionSpace, componentSpace, moduleConfig.getDefinitionProviderConfigs()))
				.map(supplier -> supplier.get(definitionSpace));
	}

	private static Stream<DefinitionSupplier> provide(final DefinitionSpace definitionSpace, final ComponentSpace componentSpace, final List<DefinitionProviderConfig> definitionProviderConfigs) {
		return definitionProviderConfigs
				.stream()
				.map(config -> createDefinitionProvider(componentSpace, config))
				.flatMap(definitionProvider -> definitionProvider.get(definitionSpace).stream());
	}

	private static DefinitionProvider createDefinitionProvider(final ComponentSpace componentSpace, final DefinitionProviderConfig definitionProviderConfig) {
		final DefinitionProvider instance = ComponentLoader.createInstance(definitionProviderConfig.getDefinitionProviderClass(), componentSpace, Optional.empty(),
				definitionProviderConfig.getParams());
		definitionProviderConfig.getDefinitionResourceConfigs()
				.forEach(config -> instance.addDefinitionResourceConfig(config));
		return instance;

	}
}
