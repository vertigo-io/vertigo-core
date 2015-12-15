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
package io.vertigo.app.config;

import io.vertigo.core.component.aop.Aspect;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Component;
import io.vertigo.lang.Option;
import io.vertigo.lang.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	//In the case of the boot module	
	private final boolean boot;
	private final AppConfigBuilder myAppConfigBuilder;
	private final String myName;

	private final List<ComponentConfigBuilder> myComponentConfigBuilders = new ArrayList<>();
	private final List<PluginConfigBuilder> plugins = new ArrayList<>();
	private final List<AspectConfig> myAspectConfigs = new ArrayList<>();
	private final List<DefinitionResourceConfig> myDefinitionResourceConfigs = new ArrayList<>();
	private final List<DefinitionProviderConfig> myDefinitionProviderConfigs = new ArrayList<>();

	private boolean myHasApi = true; //par défaut on a une api.

	/**
	 * Constructor of the boot module.
	 * @param appConfigBuilder the builder of the appConfig 
	 */
	ModuleConfigBuilder(final AppConfigBuilder appConfigBuilder) {
		Assertion.checkNotNull(appConfigBuilder);
		//-----
		myName = "boot";
		boot = true;
		myAppConfigBuilder = appConfigBuilder;
	}

	/**
	 * Constructor of a module.
	 * @param appConfigBuilder the builder of the appConfig 
	 * @param name Name of the module
	 */
	ModuleConfigBuilder(final AppConfigBuilder appConfigBuilder, final String name) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkArgument(!"boot".equalsIgnoreCase(name), "boot is a reserved name");
		Assertion.checkArgNotEmpty(name);
		//-----
		boot = false;
		myName = name;
		myAppConfigBuilder = appConfigBuilder;
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
	* Adds a distributed component.
	* @param apiClass api of the component
	* @return  the builder of the component
	*/
	public ComponentConfigBuilder beginElasticComponent(final Class<? extends Component> apiClass) {
		return doBeginComponent(Option.<Class<? extends Component>> some(apiClass), Component.class, true);
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
	public ComponentConfigBuilder beginComponent(final Class<? extends Component> implClass) {
		return doBeginComponent(Option.<Class<? extends Component>> none(), implClass, false);
	}

	/**
	* Begins the builder of a component.
	* @param apiClass api of the component
	* Component is added when you close the builder uising end() method. 
	* @param implClass impl of the component
	* @return  the builder of the component
	*/
	public ComponentConfigBuilder beginComponent(final Class<? extends Component> apiClass, final Class<? extends Component> implClass) {
		return doBeginComponent(Option.<Class<? extends Component>> some(apiClass), implClass, false);
	}

	/**
	* Adds a component defined by an api and an implementation.
	* @param apiClass api of the component
	* @param implClass impl of the component
	* @return  the builder of the component
	*/
	private ComponentConfigBuilder doBeginComponent(final Option<Class<? extends Component>> apiClass, final Class<? extends Component> implClass, final boolean elastic) {
		final ComponentConfigBuilder componentConfigBuilder = new ComponentConfigBuilder(this, apiClass, implClass, elastic);
		myComponentConfigBuilders.add(componentConfigBuilder);
		return componentConfigBuilder;
	}

	/**
	 * Ends the current module config.
	 * @return the builder of this app
	 */
	public AppConfigBuilder endModule() {
		if (boot) {
			// we don't close the module	
		} else {
			myAppConfigBuilder.addAllModules(Collections.singletonList(build()));
		}
		return myAppConfigBuilder;
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
	public PluginConfigBuilder beginPlugin(final Class<? extends Plugin> pluginImplClass) {
		final PluginConfigBuilder pluginConfigBuilder = new PluginConfigBuilder(this, pluginImplClass);
		plugins.add(pluginConfigBuilder);
		return pluginConfigBuilder;
	}

	private List<PluginConfig> buildPluginConfigs() {
		final List<PluginConfig> pluginConfigs = new ArrayList<>();
		final Set<String> pluginTypes = new HashSet<>();
		int index = 1;
		for (final PluginConfigBuilder pluginConfigBuilder : plugins) {
			final boolean added = pluginTypes.add(pluginConfigBuilder.getPluginType());
			if (added) {
				//If added, its the first plugin to this type.
				pluginConfigBuilder.withIndex(0);
			} else {
				pluginConfigBuilder.withIndex(index);
				index++;
			}

			pluginConfigs.add(pluginConfigBuilder.build());
		}
		return pluginConfigs;
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
		final List<ComponentConfig> componentConfig = new ArrayList<>();
		for (final ComponentConfigBuilder componentConfigBuilder : myComponentConfigBuilders) {
			componentConfig.add(componentConfigBuilder.build());
		}

		//creation of the pluginConfigs
		final List<PluginConfig> pluginConfigs = buildPluginConfigs();

		final ModuleConfig moduleConfig = new ModuleConfig(
				myName,
				myDefinitionProviderConfigs,
				myDefinitionResourceConfigs,
				componentConfig,
				pluginConfigs,
				myAspectConfigs,
				moduleRules);

		moduleConfig.checkRules();
		return moduleConfig;
	}

}
