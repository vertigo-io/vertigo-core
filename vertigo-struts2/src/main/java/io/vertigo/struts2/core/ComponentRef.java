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
package io.vertigo.struts2.core;

import io.vertigo.app.Home;
import io.vertigo.lang.Assertion;

import java.io.Serializable;

/**
 * Référence vers un composant.
 * Permet d'assurer le référencement du composant hors de l'injecteur.
 * Et eventuellement le référencement reporté au premier appel (lazyLoading).
 *
 * @author pchretien, npiedeloup
 * @param <T> Type du composant
 */
public final class ComponentRef<T> implements Serializable {
	private static final long serialVersionUID = -3692640153710870256L;
	private transient T instance;
	private final String componentId;
	private final Class<T> componentClazz;

	/**
	 * @param componentClazz Type du composant
	 * @return Référence vers ce composant
	 */
	public static <T> ComponentRef<T> makeRef(final Class<T> componentClazz) {
		return new ComponentRef<>(componentClazz, false);
	}

	/**
	 * @param componentClazz Type du composant
	 * @return Référence résolue en lazy loading vers ce composant
	 */
	public static <T> ComponentRef<T> makeLazyRef(final Class<T> componentClazz) {
		return new ComponentRef<>(componentClazz, true);
	}

	/**
	 * Constructeur.
	 * @param componentClazz Class du composant
	 * @param lazy Si référencement à la première demande
	 */
	private ComponentRef(final Class<T> componentClazz, final boolean lazy) {
		Assertion.checkNotNull(componentClazz);
		//-----
		componentId = null;
		this.componentClazz = componentClazz;
		if (!lazy) {
			get();
		}
	}

	/**
	 * @return Element pointé par la référence
	 */
	//le synchronized à peu d'impact sur une référence qui à vocation à être instanciée à chaque usage
	// TODO a voir si on le retire.
	public synchronized T get() {
		if (instance == null) {
			if (componentId != null) {
				instance = Home.getApp().getComponentSpace().resolve(componentId, componentClazz);
			} else {
				instance = Home.getApp().getComponentSpace().resolve(componentClazz);
			}
		}
		return instance;
	}
}
