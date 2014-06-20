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
package io.vertigo.kernel.di.configurator;

import io.vertigo.kernel.aop.Interceptor;
import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;
import io.vertigo.kernel.lang.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Paramétrage de l'application.
 * 
 * @author npiedeloup, pchretien
 */
public final class ModuleConfigBuilder implements Builder<ModuleConfig> {
	private final ComponentSpaceConfigBuilder componentSpaceConfigBuilder;
	private final String name;
	private final List<ComponentConfigBuilder> componentConfigBuilders = new ArrayList<>();
	private final List<AspectConfig> aspectConfigs = new ArrayList<>();
	private final Map<String, String> resources = new HashMap<>(); /*path, type*/

	//---Rules
	private boolean hasApi = true; //par défaut on a une api.
	private Class<?> superClass = Manager.class; //Par défaut la super Classe est Manager

	ModuleConfigBuilder(final ComponentSpaceConfigBuilder componentSpaceConfigBuilder, final String name) {
		Assertion.checkNotNull(componentSpaceConfigBuilder);
		Assertion.checkArgNotEmpty(name);
		//---------------------------------------------------------------------
		this.name = name;
		this.componentSpaceConfigBuilder = componentSpaceConfigBuilder;
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
		resources.put(resourcePath, resourceType);
		return this;
	}

	public ModuleConfigBuilder withAspect(final Class<?> annotationType, final Class<? extends Interceptor> implClass) {
		aspectConfigs.add(new AspectConfig(annotationType, implClass));
		return this;
	}

	public ModuleConfigBuilder withNoAPI() {
		hasApi = false;
		return this;
	}

	public ModuleConfigBuilder withInheritance(final Class<?> newSuperClass) {
		Assertion.checkNotNull(newSuperClass);
		//---------------------------------------------------------------------
		superClass = newSuperClass;
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
		componentConfigBuilders.add(componentConfigBuilder);
		return componentConfigBuilder;
	}

	public ComponentSpaceConfigBuilder endModule() {
		return componentSpaceConfigBuilder;
	}

	/** {@inheritDoc} */
	public ModuleConfig build() {
		final List<ModuleRule> moduleRules = new ArrayList<>();
		//Mise à jour des règles. 
		if (hasApi) {
			moduleRules.add(new APIModuleRule());
		}
		moduleRules.add(new InheritanceModuleRule(superClass));
		//-----
		final List<ComponentConfig> componentConfig = new ArrayList<>();
		for (final ComponentConfigBuilder componentConfigBuilder : componentConfigBuilders) {
			componentConfig.add(componentConfigBuilder.build());
		}
		final ModuleConfig moduleConfig = new ModuleConfig(name, componentConfig, aspectConfigs, moduleRules, resources);
		moduleConfig.checkRules();
		return moduleConfig;
	}

}
