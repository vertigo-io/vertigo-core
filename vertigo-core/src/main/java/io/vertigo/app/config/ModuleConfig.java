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
package io.vertigo.app.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertigo.app.config.rules.ModuleRule;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;
import io.vertigo.util.ListBuilder;

/**
 * Configuration of a module.
 * This config module contains
 *  - config of components
 *  - config of plugins
 *  - config of resources
 *  - params
 *  - rules
 *
 * @author npiedeloup, pchretien
 */
public final class ModuleConfig {
	private final String name;
	private final List<DefinitionProviderConfig> definitionProviders;
	private final List<ComponentConfig> components;
	private final List<PluginConfig> plugins;
	private final List<AspectConfig> aspects;
	@JsonExclude
	private final List<ModuleRule> moduleRules;

	ModuleConfig(final String name,
			final List<DefinitionProviderConfig> definitionProviderConfigs,
			final List<ComponentConfig> componentConfigs,
			final List<PluginConfig> pluginConfigs,
			final List<AspectConfig> aspectConfigs,
			final List<ModuleRule> moduleRules) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(definitionProviderConfigs);
		Assertion.checkNotNull(componentConfigs);
		Assertion.checkNotNull(pluginConfigs);
		Assertion.checkNotNull(aspectConfigs);
		Assertion.checkNotNull(moduleRules);
		//-----
		this.name = name;
		definitionProviders = Collections.unmodifiableList(new ArrayList<>(definitionProviderConfigs));
		components = Collections.unmodifiableList(new ArrayList<>(componentConfigs));
		plugins = Collections.unmodifiableList(new ArrayList<>(pluginConfigs));
		aspects = aspectConfigs;
		this.moduleRules = Collections.unmodifiableList(new ArrayList<>(moduleRules));
	}

	public List<DefinitionProviderConfig> getDefinitionProviderConfigs() {
		return definitionProviders;
	}

	/**
	 * @return the list of the component-configs
	 */
	public List<ComponentConfig> getComponentConfigs() {
		return new ListBuilder<ComponentConfig>()
				.addAll(components)
				.addAll(ConfigUtil.buildConfigs(plugins))
				.build();
	}

	/**
	 * @return the list of the aspect-configs
	 */
	public List<AspectConfig> getAspectConfigs() {
		return aspects;
	}

	/**
	 * @return Nom du module.
	 */
	public String getName() {
		return name;
	}

	void checkRules() {
		for (final ModuleRule moduleRule : moduleRules) {
			moduleRule.check(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return name;
	}
}
