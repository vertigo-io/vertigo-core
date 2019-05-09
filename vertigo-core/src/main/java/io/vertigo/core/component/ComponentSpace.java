/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.component;

import java.util.Collections;
import java.util.Optional;

import io.vertigo.app.Home;
import io.vertigo.core.component.loader.ComponentSpaceLoader;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Centralisation des accès aux composants et aux plugins.
 * @author pchretien
 */
public interface ComponentSpace extends Container {
	/**
	 * Resolve a component from its class.
	 * @param componentClass Type of the component
	 * @return Component
	 */
	default <C> C resolve(final Class<C> componentClass) {
		final String normalizedId = StringUtil.first2LowerCase(componentClass.getSimpleName());
		return resolve(normalizedId, componentClass);
	}

	/**
	 * Creates an new object instance of the given class and inject dependencies using the current App ComponentSpace as container.
	 * This created object is not registered in the ComponantSpace.
	 * Therefore the clazz cannot implement the interface Activeable or DefinitionProvider because the lifecycle of this component is not handled by Vertigo.
	 * @param clazz the clazz of the object your want to create with it's member injected.
	 * @return the newly created object.
	 */
	public static <T> T newInstance(final Class<T> clazz) {
		Assertion.checkNotNull(clazz);
		// ---
		return ComponentSpaceLoader.createInstance(clazz, Home.getApp().getComponentSpace(), Optional.empty(), Collections.emptyMap());
	}

	/**
	 * Inject dependencies in the instance using the current App ComponentSpace as container.
	 * @param instance the object your want to get it's member injected.
	 * @return the enhanced object.
	 */
	public static void injectMembers(final Object instance) {
		Assertion.checkNotNull(instance);
		//-----
		ComponentSpaceLoader.injectMembers(instance, Home.getApp().getComponentSpace(), Optional.empty(), Collections.emptyMap());
	}

}
