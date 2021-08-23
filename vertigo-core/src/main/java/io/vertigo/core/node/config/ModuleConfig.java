/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2021, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.config;

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.ListBuilder;

/**
 * Configuration of a module.
 * This config module contains
 *  - config of components
 *  - config of plugins
 *  - config of resources
 *  - params
 *
 * @author npiedeloup, pchretien
 */
public record ModuleConfig(
		String name,
		List<DefinitionProviderConfig> definitionProviderConfigs,
		List<ComponentConfig> componentConfigs,
		List<PluginConfig> pluginConfigs,
		List<ConnectorConfig> connectorConfigs,
		List<AmplifierConfig> amplifierConfigs,
		List<AspectConfig> aspectConfigs,
		List<ProxyMethodConfig> proxyMethodConfigs) {

	public ModuleConfig {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(definitionProviderConfigs)
				.isNotNull(componentConfigs)
				.isNotNull(pluginConfigs)
				.isNotNull(connectorConfigs)
				.isNotNull(amplifierConfigs)
				.isNotNull(aspectConfigs)
				.isNotNull(proxyMethodConfigs);
		//-----
		definitionProviderConfigs = List.copyOf(definitionProviderConfigs);
		componentConfigs = List.copyOf(componentConfigs);
		pluginConfigs = List.copyOf(pluginConfigs);
		connectorConfigs = List.copyOf(connectorConfigs);
		amplifierConfigs = List.copyOf(amplifierConfigs);
		aspectConfigs = List.copyOf(aspectConfigs);
		proxyMethodConfigs = List.copyOf(proxyMethodConfigs);
	}

	/**
	 * Static method factory for ModuleConfigBuilder
	 * @param name Name of the module
	 * @return ModuleConfigBuilder
	 */
	public static ModuleConfigBuilder builder(final String name) {
		return new ModuleConfigBuilder(name);
	}

	/**
	 * @return the list of the component-configs
	 */
	public List<CoreComponentConfig> getComponentConfigs() {
		return new ListBuilder<CoreComponentConfig>()
				.addAll(ConfigUtil.buildComponentConfigs(componentConfigs()))
				.addAll(ConfigUtil.buildPluginsComponentConfigs(pluginConfigs()))
				.addAll(ConfigUtil.buildConnectorsComponentConfigs(connectorConfigs()))
				.addAll(ConfigUtil.buildAmplifiersComponentConfigs(amplifierConfigs()))
				.build();
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return name;
	}
}
