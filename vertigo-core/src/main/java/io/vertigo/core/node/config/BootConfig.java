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
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.JsonExclude;
import io.vertigo.core.node.component.AopPlugin;
import io.vertigo.core.util.ListBuilder;

/**
 * This Class defines the properties of ComponentSpace and DefinitionSpace.
 * That's to say : how to boot the modules of Vertigo.
 * @author pchretien
 */
public final class BootConfig {
	private final Optional<LogConfig> logConfigOpt;
	private final boolean isVerbose;
	@JsonExclude
	private final AopPlugin aopPlugin;

	private final List<ComponentConfig> componentConfigs;
	private final List<PluginConfig> pluginConfigs;

	/**
	 * Constructor.
	 * @param aopPlugin AopPlugin
	 * @param isVerbose if logs are enabled during startup
	 */
	BootConfig(
			final Optional<LogConfig> logConfigOpt,
			final List<ComponentConfig> componentConfigs,
			final List<PluginConfig> pluginConfigs,
			final AopPlugin aopPlugin,
			final boolean isVerbose) {
		Assertion.check()
				.isNotNull(logConfigOpt)
				.isNotNull(componentConfigs)
				.isNotNull(pluginConfigs)
				.isNotNull(aopPlugin);
		//-----
		this.logConfigOpt = logConfigOpt;
		this.componentConfigs = componentConfigs;
		this.pluginConfigs = pluginConfigs;
		this.isVerbose = isVerbose;
		this.aopPlugin = aopPlugin;
	}

	/**
	 * Static method factory for NodeConfigBuilder
	 * @return NodeConfigBuilder
	 */
	public static BootConfigBuilder builder() {
		return new BootConfigBuilder();
	}

	/**
	 * @return the logconfig
	 */
	public Optional<LogConfig> logConfigOpt() {
		return logConfigOpt;
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

	/**
	 * @return if the startup is isVerbose
	 */
	public boolean isVerbose() {
		return isVerbose;
	}

	/**
	 * @return AopEngine
	 */
	public AopPlugin aopPlugin() {
		return aopPlugin;
	}
}
