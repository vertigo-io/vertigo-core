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
import java.util.List;
import java.util.Optional;

import io.vertigo.app.config.rules.APIModuleRule;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Component;
import io.vertigo.lang.Plugin;
import io.vertigo.util.ListBuilder;

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

	private final List<ComponentConfigBuilder> myComponentConfigBuilders = new ArrayList<>();
	private final List<PluginConfigBuilder> myPluginConfigBuilders = new ArrayList<>();
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
	 * Add a component.
	 * @param implClass impl of the component
	 * @return this builder
	 */
	public ModuleConfigBuilder addComponent(final Class<? extends Component> implClass) {
		return beginComponent(implClass).endComponent();
	}

	/**
	 * Add a component.
	 * @param apiClass api of the component
	 * @param implClass impl of the component
	 * @return this builder
	 */
	public ModuleConfigBuilder addComponent(final Class<? extends Component> apiClass, final Class<? extends Component> implClass) {
		return beginComponent(apiClass, implClass).endComponent();
	}

	/**
	* Begins the builder of a component.
	* Component is added when you close the builder uising end() method.
	* @param implClass impl of the component
	* @return  the builder of the component
	*/
	public ComponentConfigBuilder<ModuleConfigBuilder> beginComponent(final Class<? extends Component> implClass) {
		return doBeginComponent(Optional.<Class<? extends Component>> empty(), implClass);
	}

	/**
	* Begins the builder of a component.
	* @param apiClass api of the component
	* Component is added when you close the builder uising end() method.
	* @param implClass impl of the component
	* @return  the builder of the component
	*/
	public ComponentConfigBuilder<ModuleConfigBuilder> beginComponent(final Class<? extends Component> apiClass, final Class<? extends Component> implClass) {
		return doBeginComponent(Optional.<Class<? extends Component>> of(apiClass), implClass);
	}

	/**
	* Adds a component defined by an api and an implementation.
	* @param apiClass api of the component
	* @param implClass impl of the component
	* @return  the builder of the component
	*/
	private ComponentConfigBuilder doBeginComponent(final Optional<Class<? extends Component>> apiClass, final Class<? extends Component> implClass) {
		final ComponentConfigBuilder componentConfigBuilder = new ComponentConfigBuilder(this, apiClass, implClass);
		myComponentConfigBuilders.add(componentConfigBuilder);
		return componentConfigBuilder;
	}

	/**
	 * Adds a plugin defined by its implementation.
	 * @param pluginImplClass  impl of the plugin
	 * @return this builder
	 */
	public ModuleConfigBuilder addPlugin(final Class<? extends Plugin> pluginImplClass) {
		return beginPlugin(pluginImplClass).endPlugin();
	}

	/**
	 * Begins the builder of a plugin.
	 * @param pluginImplClass impl of the plugin
	 * @return  the builder of the plugin
	 */
	public PluginConfigBuilder<ModuleConfigBuilder> beginPlugin(final Class<? extends Plugin> pluginImplClass) {
		final PluginConfigBuilder pluginConfigBuilder = new PluginConfigBuilder(this, pluginImplClass);
		myPluginConfigBuilders.add(pluginConfigBuilder);
		return pluginConfigBuilder;
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
		final List<ComponentConfig> componentConfigs = new ListBuilder<ComponentConfig>()
				.addAll(ConfigUtil.buildComponentConfigs(myComponentConfigBuilders))
				.addAll(ConfigUtil.buildPluginConfigs(myPluginConfigBuilders))
				.build();

		final ModuleConfig moduleConfig = new ModuleConfig(
				myName,
				myDefinitionProviderConfigs,
				myDefinitionResourceConfigs,
				componentConfigs,
				myAspectConfigs,
				moduleRules);

		moduleConfig.checkRules();
		return moduleConfig;
	}

}
