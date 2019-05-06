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
import java.util.Map;
import java.util.Optional;

import io.vertigo.app.Home;
import io.vertigo.core.component.loader.ComponentSpaceLoader;
import io.vertigo.core.param.ParamManager;
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

	public static <T> T newInstance(final Class<T> clazz) {
		Assertion.checkNotNull(clazz);
		// ---
		return newInstance(clazz, Collections.emptyMap());
	}

	public static <T> T newInstance(final Class<T> clazz, final Map<String, String> params) {
		Assertion.checkNotNull(clazz);
		Assertion.checkNotNull(params);
		// ---
		final ComponentSpace componentSpace = Home.getApp().getComponentSpace();
		final Optional<ParamManager> paramManagerOpt = Optional.of(componentSpace.resolve(ParamManager.class));
		return ComponentSpaceLoader.createInstance(clazz, componentSpace, paramManagerOpt, params);
	}

	public static void injectMembers(final Object instance) {
		Assertion.checkNotNull(instance);
		//-----
		injectMembers(instance, Collections.emptyMap());
	}

	public static void injectMembers(final Object instance, final Map<String, String> params) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(params);
		//-----
		final ComponentSpace componentSpace = Home.getApp().getComponentSpace();
		final Optional<ParamManager> paramManagerOpt = Optional.of(componentSpace.resolve(ParamManager.class));
		ComponentSpaceLoader.injectMembers(instance, componentSpace, paramManagerOpt, params);
	}

}
