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

import io.vertigo.core.aop.AOPInterceptor;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Component;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Paramétrage de l'application.
 *
 * @author npiedeloup, pchretien
 */
public final class ModuleConfigBuilder implements Builder<ModuleConfig> {
	private final AppConfigBuilder myAppConfigBuilder;
	private final String myName;
	private final List<ComponentConfigBuilder> myComponentConfigBuilders = new ArrayList<>();
	private final List<AspectConfig> myAspectConfigs = new ArrayList<>();

	//---Rules
	private boolean myHasApi = true; //par défaut on a une api.
	private Class<?> mySuperClass = Component.class; //Par défaut la super Classe est Manager

	ModuleConfigBuilder(final AppConfigBuilder appConfigBuilder, final String name) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkArgNotEmpty(name);
		//---------------------------------------------------------------------
		myName = name;
		myAppConfigBuilder = appConfigBuilder;
	}

	public ModuleConfigBuilder withAspect(final Class<?> annotationType, final Class<? extends AOPInterceptor> implClass) {
		myAspectConfigs.add(new AspectConfig(annotationType, implClass));
		return this;
	}

	public ModuleConfigBuilder withNoAPI() {
		myHasApi = false;
		return this;
	}

	public ModuleConfigBuilder withInheritance(final Class<?> superClass) {
		Assertion.checkNotNull(superClass);
		//---------------------------------------------------------------------
		mySuperClass = superClass;
		return this;
	}

	/**
	* Ajout d'un composant distribué.
	* @param apiClass Classe du composant (Interface)
	* @return Builder
	*/
	public ComponentConfigBuilder beginElasticComponent(final Class<?> apiClass) {
		return doBeginComponent(Option.<Class<?>> some(apiClass), Object.class, true);
	}

	/**
	* Ajout d'un composant.
	* @param implClass Classe d'implémentation du composant
	* @return Builder
	*/
	public ComponentConfigBuilder beginComponent(final Class<?> implClass) {
		return doBeginComponent(Option.<Class<?>> none(), implClass, false);
	}

	/**
	* Ajout d'un composant.
	* @param apiClass Classe du composant (Interface)
	* @param implClass Classe d'implémentation du composant
	* @return Builder
	*/
	public ComponentConfigBuilder beginComponent(final Class<?> apiClass, final Class<?> implClass) {
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
		return myAppConfigBuilder;
	}

	/** {@inheritDoc} */
	public ModuleConfig build() {
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
		final ModuleConfig moduleConfig = new ModuleConfig(myName, componentConfig, myAspectConfigs, moduleRules);
		moduleConfig.checkRules();
		return moduleConfig;
	}

}
