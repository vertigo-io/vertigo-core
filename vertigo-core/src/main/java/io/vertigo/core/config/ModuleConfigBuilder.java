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
package io.vertigo.core.config;

import io.vertigo.core.aop.Aspect;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Component;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Paramétrage de l'application.
 *
 * @author npiedeloup, pchretien
 */
public final class ModuleConfigBuilder implements Builder<ModuleConfig> {
	private final Option<AppConfigBuilder> myAppConfigBuilderOption;
	private final String myName;
	private final List<ComponentConfigBuilder> myComponentConfigBuilders = new ArrayList<>();
	private final List<AspectConfig> myAspectConfigs = new ArrayList<>();
	private final List<ResourceConfig> myResourceConfigs = new ArrayList<>();

	//---Rules
	private boolean myHasApi = true; //par défaut on a une api.
	private Class<?> mySuperClass = Component.class; //Par défaut la super Classe est Manager

	//State to avoid reuse of this Builder
	private boolean ended = false;

	public ModuleConfigBuilder(final String name) {
		Assertion.checkArgNotEmpty(name);
		//-----
		myName = name;
		myAppConfigBuilderOption = Option.none();
	}

	ModuleConfigBuilder(final AppConfigBuilder appConfigBuilder, final String name) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkArgNotEmpty(name);
		//-----
		myName = name;
		myAppConfigBuilderOption = Option.some(appConfigBuilder);
	}

	public ModuleConfigBuilder withAspect(final Class<? extends Aspect> implClass) {
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

	public ModuleConfigBuilder withInheritance(final Class<?> superClass) {
		Assertion.checkArgument(!ended, "this builder is ended");
		Assertion.checkNotNull(superClass);
		//-----
		mySuperClass = superClass;
		return this;
	}

	/**
	 * Ajout de resources
	 * @param resourceType Type of resource
	 */
	public ModuleConfigBuilder withResource(final String resourceType, final String resourcePath) {
		Assertion.checkArgument(!ended, "this builder is ended");
		Assertion.checkArgNotEmpty(resourceType);
		Assertion.checkNotNull(resourcePath);
		//-----
		myResourceConfigs.add(new ResourceConfig(resourceType, resourcePath));
		return this;
	}

	/**
	* Ajout d'un composant distribué.
	* @param apiClass Classe du composant (Interface)
	* @return Builder
	*/
	public ComponentConfigBuilder beginElasticComponent(final Class<?> apiClass) {
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
		return doBeginComponent(Option.<Class<?>> some(apiClass), Object.class, true);
	}

	/**
	* Ajout d'un composant.
	* @param implClass Classe d'implémentation du composant
	* @return Builder
	*/
	public ComponentConfigBuilder beginComponent(final Class<?> implClass) {
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
		return doBeginComponent(Option.<Class<?>> none(), implClass, false);
	}

	/**
	* Ajout d'un composant.
	* @param apiClass Classe du composant (Interface)
	* @param implClass Classe d'implémentation du composant
	* @return Builder
	*/
	public ComponentConfigBuilder beginComponent(final Class<?> apiClass, final Class<?> implClass) {
		Assertion.checkArgument(!ended, "this builder is ended");
		//-----
		return doBeginComponent(Option.<Class<?>> some(apiClass), implClass, false);
	}

	/**
	* Ajout d'un composant.
	* @param apiClass Classe du composant (Interface)
	* @param implClass Classe d'implémentation du composant
	* @return Builder
	*/
	private ComponentConfigBuilder doBeginComponent(final Option<Class<?>> apiClass, final Class<?> implClass, final boolean elastic) {
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
		Assertion.checkArgument(myAppConfigBuilderOption.isDefined(), "beginModule() must be used before endModule()");
		//-----
		myAppConfigBuilderOption.get().withModules(Collections.singletonList(this.build()));
		ended = true;
		return myAppConfigBuilderOption.get();
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
		moduleRules.add(new InheritanceModuleRule(mySuperClass));
		//-----
		final List<ComponentConfig> componentConfig = new ArrayList<>();
		for (final ComponentConfigBuilder componentConfigBuilder : myComponentConfigBuilders) {
			componentConfig.add(componentConfigBuilder.build());
		}
		final ModuleConfig moduleConfig = new ModuleConfig(myName, myResourceConfigs, componentConfig, myAspectConfigs, moduleRules);
		moduleConfig.checkRules();
		return moduleConfig;
	}

}
