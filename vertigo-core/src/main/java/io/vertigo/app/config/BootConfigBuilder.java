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

import io.vertigo.core.component.AopPlugin;
import io.vertigo.core.definition.loader.DefinitionLoader;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.locale.LocaleManagerImpl;
import io.vertigo.core.param.ParamManager;
import io.vertigo.core.param.ParamManagerImpl;
import io.vertigo.core.plugins.component.aop.cglib.CGLIBAopPlugin;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.core.resource.ResourceManagerImpl;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Component;
import io.vertigo.lang.Plugin;
import io.vertigo.util.ListBuilder;

/**
 * Configuration.
 *
 * @author npiedeloup, pchretien
 */
public final class BootConfigBuilder implements Builder<BootConfig> {
	private Optional<LogConfig> myLogConfigOption = Optional.empty(); //par défaut
	private final AppConfigBuilder appConfigBuilder;
	private boolean mySilence; //false by default
	private AopPlugin myAopPlugin = new CGLIBAopPlugin(); //By default
	private final List<ComponentConfigBuilder> myComponentConfigBuilders = new ArrayList<>();
	private final List<PluginConfigBuilder> myPluginConfigBuilders = new ArrayList<>();

	/**
	 * @param appConfigBuilder Parent AppConfig builder
	 */
	BootConfigBuilder(final AppConfigBuilder appConfigBuilder) {
		Assertion.checkNotNull(appConfigBuilder);
		//-----
		this.appConfigBuilder = appConfigBuilder;
	}

	/**
	 * Opens the boot module.
	 * There is exactly one BootConfig per AppConfig.
	 *
	 * @param locales a string which contains all the locales separated with a simple comma : ',' .
	 * @return this builder
	 */
	public BootConfigBuilder withLocales(final String locales) {
		beginComponent(LocaleManager.class, LocaleManagerImpl.class)
				.addParam("locales", locales)
				.endComponent();
		return this;
	}

	/**
	 * Ajout de paramètres
	 * @param logConfig Config of logs
	 * @return this builder
	 */
	public BootConfigBuilder withLogConfig(final LogConfig logConfig) {
		Assertion.checkNotNull(logConfig);
		//-----
		myLogConfigOption = Optional.of(logConfig);
		return this;
	}

	/**
	 * Permet de définir un démarrage silencieux. (Sans retour console)
	 * @return this builder
	 */
	public BootConfigBuilder silently() {
		mySilence = true;
		return this;
	}

	/**
	 * @param aopPlugin AopPlugin
	 * @return this builder
	 */
	public BootConfigBuilder withAopEngine(final AopPlugin aopPlugin) {
		Assertion.checkNotNull(aopPlugin);
		//-----
		myAopPlugin = aopPlugin;
		return this;
	}

	/**
	 * @return AppConfig builder
	 */
	public AppConfigBuilder endBoot() {
		return appConfigBuilder;
	}

	/**
	* Begins the builder of a component.
	* Component is added when you close the builder uising end() method.
	* @param implClass impl of the component
	* @return  the builder of the component
	*/
	private ComponentConfigBuilder<BootConfigBuilder> beginComponent(final Class<? extends Component> implClass) {
		return doBeginComponent(Optional.<Class<? extends Component>> empty(), implClass);
	}

	/**
	* Begins the builder of a component.
	* @param apiClass api of the component
	* Component is added when you close the builder uising end() method.
	* @param implClass impl of the component
	* @return  the builder of the component
	*/
	private ComponentConfigBuilder<BootConfigBuilder> beginComponent(final Class<? extends Component> apiClass, final Class<? extends Component> implClass) {
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
	public BootConfigBuilder addPlugin(final Class<? extends Plugin> pluginImplClass) {
		return beginPlugin(pluginImplClass).endPlugin();
	}

	/**
	 * Begins the builder of a plugin.
	 * @param pluginImplClass impl of the plugin
	 * @return  the builder of the plugin
	 */
	public PluginConfigBuilder<BootConfigBuilder> beginPlugin(final Class<? extends Plugin> pluginImplClass) {
		final PluginConfigBuilder pluginConfigBuilder = new PluginConfigBuilder(this, pluginImplClass);
		myPluginConfigBuilders.add(pluginConfigBuilder);
		return pluginConfigBuilder;
	}

	/**
	 * @return BootConfig
	 */
	@Override
	public BootConfig build() {
		beginComponent(ResourceManager.class, ResourceManagerImpl.class).endComponent()
				.beginComponent(ParamManager.class, ParamManagerImpl.class).endComponent()
				.beginComponent(DefinitionLoader.class).endComponent();

		final List<ComponentConfig> componentConfigs = new ListBuilder<ComponentConfig>()
				.addAll(ConfigUtil.buildComponentConfigs(myComponentConfigBuilders))
				.addAll(ConfigUtil.buildPluginConfigs(myPluginConfigBuilders))
				.build();

		return new BootConfig(
				myLogConfigOption,
				componentConfigs,
				myAopPlugin,
				mySilence);
	}
}
