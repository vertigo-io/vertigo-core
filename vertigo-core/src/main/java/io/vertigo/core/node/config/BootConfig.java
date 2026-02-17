/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.ListBuilder;
import io.vertigo.core.lang.json.JsonExclude;
import io.vertigo.core.node.component.AspectPlugin;

/**
 * This Class defines the properties of ComponentSpace and DefinitionSpace.
 * That's to say : how to boot the modules of Vertigo.
 * @author pchretien
 */
public record BootConfig(
		Optional<LogConfig> logConfigOpt,
		List<ComponentConfig> componentConfigs,
		List<PluginConfig> pluginConfigs,
		@JsonExclude AspectPlugin aspectPlugin,
		boolean isVerbose) {

	public BootConfig {
		Assertion.check()
				.isNotNull(logConfigOpt)
				.isNotNull(componentConfigs)
				.isNotNull(pluginConfigs)
				.isNotNull(aspectPlugin);
		//-----
		componentConfigs = List.copyOf(componentConfigs);
		pluginConfigs = List.copyOf(pluginConfigs);
	}

	/**
	 * Static method factory for NodeConfigBuilder
	 * @return NodeConfigBuilder
	 */
	public static BootConfigBuilder builder() {
		return new BootConfigBuilder();
	}

	/**
	 * @return the list of component-configs
	 */
	public List<CoreComponentConfig> coreComponentConfigs() {
		return new ListBuilder<CoreComponentConfig>()
				.addAll(ConfigUtil.buildComponentConfigs(componentConfigs))
				.addAll(ConfigUtil.buildPluginsComponentConfigs(pluginConfigs))
				.build();
	}
}
