/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.component.di;

import java.lang.reflect.Constructor;

import jakarta.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.StringUtil;

/**
 * @author prahmoune
 */
public final class DIAnnotationUtil {
	private DIAnnotationUtil() {
		//Utility class, constructor is private.
	}

	/**
	 * Finds the injectable constructor.
	 * There must be exactly one public constructor.
	 * This constructor must be either empty or annotated with @Inject.
	 * @param clazz Class of the object
	 * @return Constructor of the object
	 */
	static <T> Constructor<T> findInjectableConstructor(final Class<T> clazz) {
		Assertion.check().isNotNull(clazz);
		//-----
		final Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
		Assertion.check()
				.isNotNull(constructors, "No identifiable public constructor")
				.isTrue(constructors.length == 1, "Exactly one public constructor must be declared on {0}", clazz.getName())
				.isTrue(isInjectable(constructors[0]), "The public constructor of {0} must be annotated with @Inject or be empty", clazz.getName());
		//-----
		//There is exactly one constructor.
		return constructors[0];
	}

	/*
	 * A constructor is "injectable" if
	 * - it has no param  - default constructor-
	 * - it is annotated with @Inject
	 */
	private static boolean isInjectable(final Constructor<?> constructor) {
		return constructor.getParameterTypes().length == 0 || constructor.isAnnotationPresent(Inject.class);
	}

	/**
	 * Builds an ID for a component defined by its implementation class.
	 * @param clazz Implementation class of the component
	 * @return Component identifier
	 */
	public static String buildId(final Class<?> clazz) {
		Assertion.check().isNotNull(clazz);
		//-----
		//Build the component identifier.
		//By convention, we use the class simple name.
		return StringUtil.first2LowerCase(clazz.getSimpleName());
	}

}
