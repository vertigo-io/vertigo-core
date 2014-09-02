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
package io.vertigo.core.di.configurator;

import io.vertigo.core.aop.Interceptor;
import io.vertigo.core.component.Manager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.lang.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Paramétrage de l'application.
 * 
 * @author npiedeloup, pchretien
 */
public final class ModuleConfigBuilder implements Builder<ModuleConfig> {
	private final ComponentSpaceConfigBuilder myComponentSpaceConfigBuilder;
	private final String myName;
	private final List<ComponentConfigBuilder> myComponentConfigBuilders = new ArrayList<>();
	private final List<AspectConfig> myAspectConfigs = new ArrayList<>();
	private final List<ResourceConfig> myResourceConfigs = new ArrayList<>();

	//---Rules
	private boolean myHasApi = true; //par défaut on a une api.
	private Class<?> mySuperClass = Manager.class; //Par défaut la super Classe est Manager

	ModuleConfigBuilder(final ComponentSpaceConfigBuilder componentSpaceConfigBuilder, final String name) {
		Assertion.checkNotNull(componentSpaceConfigBuilder);
		Assertion.checkArgNotEmpty(name);
		//---------------------------------------------------------------------
		myName = name;
		myComponentSpaceConfigBuilder = componentSpaceConfigBuilder;
	}

	/**
	 * Ajout de resources  
	 * @param paramName Nom du paramètre
	 * @param paramValue Valeur du paramètre
	 * @return
	 */
	public ModuleConfigBuilder withResource(final String resourceType, final String resourcePath) {
		Assertion.checkArgNotEmpty(resourceType);
		Assertion.checkNotNull(resourcePath);
		//---------------------------------------------------------------------
		myResourceConfigs.add(new ResourceConfig(resourceType, resourcePath));
		return this;
	}

	public ModuleConfigBuilder withAspect(final Class<?> annotationType, final Class<? extends Interceptor> implClass) {
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
	public ComponentSpaceConfigBuilder endModule() {
		return myComponentSpaceConfigBuilder;
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
		final ModuleConfig moduleConfig = new ModuleConfig(myName, componentConfig, myAspectConfigs, moduleRules, myResourceConfigs);
		moduleConfig.checkRules();
		return moduleConfig;
	}

}
