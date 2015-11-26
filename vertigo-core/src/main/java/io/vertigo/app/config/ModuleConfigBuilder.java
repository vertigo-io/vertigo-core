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
 * Paramétrage de l'application.
 *
 * @author npiedeloup, pchretien
 */
public final class ModuleConfigBuilder implements Builder<ModuleConfig> {
	//In the case of the boot module	
	private final boolean boot;
	private final AppConfigBuilder myAppConfigBuilder;
	private final String myName;
	private final List<ComponentConfigBuilder> myComponentConfigBuilders = new ArrayList<>();
	private final List<AspectConfig> myAspectConfigs = new ArrayList<>();
	private final List<DefinitionResourceConfig> myDefinitionResourceConfigs = new ArrayList<>();
	private final List<DefinitionProviderConfig> myDefinitionProviderConfigs = new ArrayList<>();
	private final List<PluginConfigBuilder> plugins = new ArrayList<>();

	private boolean myHasApi = true; //par défaut on a une api.

	//State to avoid reuse of this Builder
	private boolean ended = false;

	ModuleConfigBuilder(final AppConfigBuilder appConfigBuilder) {
		Assertion.checkNotNull(appConfigBuilder);
		//-----
		myName = "boot";
		boot = true;
		myAppConfigBuilder = appConfigBuilder;
	}

	ModuleConfigBuilder(final AppConfigBuilder appConfigBuilder, final String name) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkArgument(!"boot".equalsIgnoreCase(name), "boot is a reserved name");
		Assertion.checkArgNotEmpty(name);
		//-----
		boot = false;
		myName = name;
		myAppConfigBuilder = appConfigBuilder;
	}

	public ModuleConfigBuilder addAspect(final Class<? extends Aspect> implClass) {
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
		myAspectConfigs.add(new AspectConfig(implClass));
		return this;
	}

	public ModuleConfigBuilder withNoAPI() {
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
		myHasApi = false;
		return this;
	}

	/**
	 * Ajout de définitions définie par un iterable.
	 */
	public ModuleConfigBuilder addDefinitionProvider(final Class<? extends DefinitionProvider> definitionProviderClass) {
		Assertion.checkArgument(!ended, "this builder is ended");
		Assertion.checkNotNull(definitionProviderClass);
		//-----
		myDefinitionProviderConfigs.add(new DefinitionProviderConfig(definitionProviderClass));
		return this;
	}

	/**
	 * Ajout de resources
	 * @param resourceType Type of resource
	 */
	public ModuleConfigBuilder addDefinitionResource(final String resourceType, final String resourcePath) {
		Assertion.checkArgument(!ended, "this builder is ended");
		Assertion.checkArgNotEmpty(resourceType);
		Assertion.checkNotNull(resourcePath);
		//-----
		myDefinitionResourceConfigs.add(new DefinitionResourceConfig(resourceType, resourcePath));
		return this;
	}

	/**
	* Ajout d'un composant distribué.
	* @param apiClass Classe du composant (Interface)
	* @return Builder
	*/
	public ComponentConfigBuilder beginElasticComponent(final Class<? extends Component> apiClass) {
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
		return doBeginComponent(Option.<Class<? extends Component>> some(apiClass), Component.class, true);
	}

	/**
	 * Add a component.
	 * @param implClass impl of the component
	 * @return Builder
	 */
	public ModuleConfigBuilder addComponent(final Class<? extends Component> implClass) {
		return beginComponent(implClass).endComponent();
	}

	/**
	 * Add a component.
	 * @param apiClass api of the component
	 * @param implClass impl of the component
	 * @return Builder
	 */
	public ModuleConfigBuilder addComponent(final Class<? extends Component> apiClass, final Class<? extends Component> implClass) {
		return beginComponent(apiClass, implClass).endComponent();
	}

	/**
	* Open the builder of a component.
	* Component is added when you close the builder uising end() method. 
	* @param implClass impl of the component
	* @return Builder
	*/
	public ComponentConfigBuilder beginComponent(final Class<? extends Component> implClass) {
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
		return doBeginComponent(Option.<Class<? extends Component>> none(), implClass, false);
	}

	/**
	* Open the builder of a component.
	* @param apiClass api of the component
	* Component is added when you close the builder uising end() method. 
	* @param implClass impl of the component
	* @return Builder
	*/
	public ComponentConfigBuilder beginComponent(final Class<? extends Component> apiClass, final Class<? extends Component> implClass) {
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
		return doBeginComponent(Option.<Class<? extends Component>> some(apiClass), implClass, false);
	}

	/**
	* Ajout d'un composant.
	* @param apiClass Classe du composant (Interface)
	* @param implClass Classe d'implémentation du composant
	* @return Builder
	*/
	private ComponentConfigBuilder doBeginComponent(final Option<Class<? extends Component>> apiClass, final Class<? extends Component> implClass, final boolean elastic) {
		final ComponentConfigBuilder componentConfigBuilder = new ComponentConfigBuilder(this, apiClass, implClass, elastic);
		myComponentConfigBuilders.add(componentConfigBuilder);
		return componentConfigBuilder;
	}

	/**
	 * Mark end of current module.
	 * @return Builder
	 */
	public AppConfigBuilder endModule() {
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
		if (boot) {
			// we don't close the module	
		} else {
			myAppConfigBuilder.withModules(Collections.singletonList(build()));
			ended = true;
		}
		return myAppConfigBuilder;
	}

	public ModuleConfigBuilder addPlugin(final Class<? extends Plugin> pluginImplClass) {
		return beginPlugin(pluginImplClass).endPlugin();
	}

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
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
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

		//Création des pluginConfigs
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
