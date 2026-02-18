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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import jakarta.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.ListBuilder;
import io.vertigo.core.util.ClassUtil;

/**
 * Model of a component.
 * A component is defined by its external dependencies.
 * Dependencies on objects provided by params are not exposed (they are not needed for resolution).
 * @author prahmoune, pchretien
 */
record DIComponentInfo(
		String id,
		Class<?> implClass,
		Collection<DIDependency> dependencies) {

	/**
	 * Constructor.
	 * @param id id
	 * @param implClass class
	 * @param params parameters
	 */
	DIComponentInfo(final String id, final Class<?> implClass, final Set<String> params) {
		this(id, implClass, buildDependencies(implClass, params));
		Assertion.check()
				.isNotBlank(id)
				.isNotNull(implClass)
				.isNotNull(params);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		//Used to display error messages during DI resolution
		return id + "<" + implClass.getSimpleName() + ">";
	}

	/*
	 * Build Dependencies
	 */
	private static Collection<DIDependency> buildDependencies(final Class<?> implClass, final Set<String> params) {
		final ListBuilder<DIDependency> dependenciesBuilder = new ListBuilder<>();
		//Parameters are assumed to be known and are therefore not part of the dependency analysis
		populateConstructorDependencies(dependenciesBuilder, implClass, params);
		populateFieldDependencies(dependenciesBuilder, implClass, params);
		return dependenciesBuilder.unmodifiable().build();
	}

	/**
	 * Dependencies on constructor.
	 */
	private static void populateConstructorDependencies(final ListBuilder<DIDependency> dependenciesBuilder, final Class<?> implClass, final Set<String> params) {
		final Constructor<?> constructor = DIAnnotationUtil.findInjectableConstructor(implClass);
		//Build the list of its dependencies.
		for (int i = 0; i < constructor.getParameterTypes().length; i++) {
			final DIDependency dependency = new DIDependency(constructor, i);
			if (!params.contains(dependency.getName())) {
				dependenciesBuilder.add(dependency);
			}
		}
	}

	/**
	 * Dependencies on each field
	 */
	private static void populateFieldDependencies(final ListBuilder<DIDependency> dependenciesBuilder, final Class<?> implClass, final Set<String> params) {
		final Collection<Field> fields = ClassUtil.getAllFields(implClass, Inject.class);
		for (final Field field : fields) {
			//Build dependency from field with allowed options.
			final DIDependency dependency = new DIDependency(field);
			if (!params.contains(dependency.getName())) {
				dependenciesBuilder.add(dependency);
			}
		}
	}
}
