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

import io.vertigo.core.component.di.DIAnnotationUtil;
import io.vertigo.core.spaces.component.ComponentInitializer;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.HashMap;
import java.util.Map;

/**
 * Paramétrage d'un composant.
 * - nom du compposant
 * - class du composant
 * - api du composant (option)
 * - liste des Plugins
 * @author npiedeloup, pchretien
 */
public final class ComponentConfig {
	private final String id;
	private final Option<Class<?>> apiClass;
	private final Class<?> implClass;
	private final Map<String, String> params;
	private final Class<? extends ComponentInitializer<?>> componentInitializerClass;
	private final boolean elastic;

	/**
	 * @param apiClass Class de l'api du composant
	 * @param implClass Class de l'implémentation du composant
	 * @param componentInitializerClass Class de l'initialiseur du composant
	 * @param pluginConfigurations Liste des plugins du composant
	 * @param params paramètres du composant
	 */
	ComponentConfig(final Option<Class<?>> apiClass, final Class<?> implClass, final boolean elastic, final Class<? extends ComponentInitializer<?>> componentInitializerClass, final Map<String, String> params) {
		Assertion.checkNotNull(apiClass);
		Assertion.checkNotNull(implClass);
		Assertion.checkNotNull(params);
		//-----
		id = apiClass.isDefined() ? DIAnnotationUtil.buildId(apiClass.get()) : DIAnnotationUtil.buildId(implClass);
		this.elastic = elastic;
		this.componentInitializerClass = componentInitializerClass;
		//-----
		this.apiClass = apiClass;
		this.implClass = implClass;
		this.params = new HashMap<>(params);
	}

	/**
	 * @return Classe d'initialisation du composant. (Nullable)
	 */
	public Class<? extends ComponentInitializer<?>> getInitializerClass() {
		return componentInitializerClass;
	}

	/**
	 * @return Classe de l'implémentation du composant
	 */
	public Class<?> getImplClass() {
		return implClass;
	}

	/**
	 * @return API du composant
	 */
	public Option<Class<?>> getApiClass() {
		return apiClass;
	}

	/**
	 * @return Identifiant du composant
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Map des paramètres du composant
	 */
	public Map<String, String> getParams() {
		return params;
	}

	public boolean isElastic() {
		return elastic;
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return getId();
	}
}
