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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.app.config.rules.APIModuleRule;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.di.DIAnnotationUtil;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Component;
import io.vertigo.lang.Plugin;

/**
 * The moduleConfigBuilder defines the configuration of a module.
 * A module has a name.
 * A module is composed of
 *  - components & plugins
 *  - aspects
 *  - definitions (defined by resources or providers)
 *
 * @author npiedeloup, pchretien
 */
public final class ModuleConfigBuilder implements Builder<ModuleConfig> {
	private final String myName;

	private final List<ComponentConfig> myComponentConfigs = new ArrayList<>();
	private final List<PluginConfig> myPluginConfigs = new ArrayList<>();
	private final List<AspectConfig> myAspectConfigs = new ArrayList<>();
	private final List<DefinitionResourceConfig> myDefinitionResourceConfigs = new ArrayList<>();
	private final List<DefinitionProviderConfig> myDefinitionProviderConfigs = new ArrayList<>();

	private boolean myHasApi = true; //par défaut on a une api.

	/**
	 * Constructor.
	 * @param name Name of the module
	 */
	public ModuleConfigBuilder(final String name) {
		Assertion.checkArgument(!"boot".equalsIgnoreCase(name), "boot is a reserved name");
		Assertion.checkArgNotEmpty(name);
		//-----
		myName = name;
	}

	/**
	 * Adds an aspect.
	 * @param implClass Class of the aspect
	 * @return this builder
	 */
	public ModuleConfigBuilder addAspect(final Class<? extends Aspect> implClass) {
		myAspectConfigs.add(new AspectConfig(implClass));
		return this;
	}

	/**
	 * Marks this module as having no api.
	 * @return this builder
	 */
	public ModuleConfigBuilder withNoAPI() {
		myHasApi = false;
		return this;
	}

	/**
	 * Adds a provider of definitions.
	 * @param definitionProviderClass Class of the definitions provider
	 * @return this builder
	 */
	public ModuleConfigBuilder addDefinitionProvider(final Class<? extends DefinitionProvider> definitionProviderClass) {
		Assertion.checkNotNull(definitionProviderClass);
		//-----
		myDefinitionProviderConfigs.add(new DefinitionProviderConfig(definitionProviderClass));
		return this;
	}

	/**
	 * Adds definitions defined by a resource file.
	 * @param resourceType Type of the resource
	 * @param resourcePath Path of the resource
	* @return this builder
	 */
	public ModuleConfigBuilder addDefinitionResource(final String resourceType, final String resourcePath) {
		Assertion.checkArgNotEmpty(resourceType);
		Assertion.checkNotNull(resourcePath);
		//-----
		myDefinitionResourceConfigs.add(new DefinitionResourceConfig(resourceType, resourcePath));
		return this;
	}

	/**
	* Adds a component defined by an implementation.
	 * @param implClass impl of the component
	 * @param params the list of params
	 * @return this builder
	 */
	public ModuleConfigBuilder addComponent(final Class<? extends Component> implClass, final Param... params) {
		Assertion.checkNotNull(implClass);
		Assertion.checkNotNull(params);
		//---
		final String id = DIAnnotationUtil.buildId(implClass);
		return addComponent(new ComponentConfig(id, Optional.empty(), implClass, Arrays.asList(params)));
	}

	/**
	* Adds a component defined by an api and an implementation.
	 * @param apiClass api of the component
	 * @param implClass impl of the component
	 * @param params the list of params
	 * @return this builder
	 */
	public ModuleConfigBuilder addComponent(final Class<? extends Component> apiClass, final Class<? extends Component> implClass, final Param... params) {
		Assertion.checkNotNull(apiClass);
		Assertion.checkNotNull(implClass);
		Assertion.checkNotNull(params);
		//---
		final String id = DIAnnotationUtil.buildId(apiClass);
		return addComponent(new ComponentConfig(id, Optional.of(apiClass), implClass, Arrays.asList(params)));
	}

	/**
	* Adds a component defined by its config.
	 * @param componentConfig the config of the component
	 * @return this builder
	 */
	public ModuleConfigBuilder addComponent(final ComponentConfig componentConfig) {
		Assertion.checkNotNull(componentConfig);
		//---
		myComponentConfigs.add(componentConfig);
		return this;
	}

	/**
	* Adds a plugin  defined by its config.
	 * @param pluginConfig the plugin-config
	 * @return this builder
	 */
	public ModuleConfigBuilder addPlugin(final PluginConfig pluginConfig) {
		Assertion.checkNotNull(pluginConfig);
		//---
		myPluginConfigs.add(pluginConfig);
		return this;
	}

	/**
	 * Adds a plugin defined by its implementation.
	 * @param pluginImplClass  impl of the plugin
	 * @param params  the list of params
	 * @return this builder
	 */
	public ModuleConfigBuilder addPlugin(final Class<? extends Plugin> pluginImplClass, final Param... params) {
		return this.addPlugin(new PluginConfig(pluginImplClass, Arrays.asList(params)));
	}

	/** {@inheritDoc} */
	@Override
	public ModuleConfig build() {
		final List<ModuleRule> moduleRules = new ArrayList<>();
		//Mise à jour des règles.
		if (myHasApi) {
			moduleRules.add(new APIModuleRule());
		}
		//-----
		final ModuleConfig moduleConfig = new ModuleConfig(
				myName,
				myDefinitionProviderConfigs,
				myDefinitionResourceConfigs,
				myComponentConfigs,
				myPluginConfigs,
				myAspectConfigs,
				moduleRules);

		moduleConfig.checkRules();
		return moduleConfig;
	}

}
