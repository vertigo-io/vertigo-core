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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.ClassUtil;

/**
 * A component has a list of dependencies.
 * A dependency is a relation between a component definition and another component definition, identified by its id.
 * A dependency can be of type:
 * - a simple class (required),
 * - an Optional,
 * - a List.
 * Only the first type is mandatory.
 *
 * @author pchretien
 */
final class DIDependency {
	private final String targetId;
	private final boolean isOptional;
	private final boolean isList;
	private final Class<?> type;

	/**
	 * Constructor for field injection.
	 * @param field Field to inject into
	 */
	DIDependency(final Field field) {
		Assertion.check().isNotNull(field);
		//-----
		final String named = getNamedValue(field.getAnnotations());
		final Class<?> rootType = field.getType();

		isOptional = caseOptional(rootType);
		isList = caseList(rootType);
		type = (isOptional || isList) ? ClassUtil.getGeneric(field) : rootType;
		Assertion.check().isNotNull(type);
		targetId = named != null ? named : DIAnnotationUtil.buildId(type);
	}

	/**
	 * Constructor for constructor parameter injection.
	 * @param constructor Constructor to inject into
	 * @param i parameter index to inject into
	 */
	DIDependency(final Constructor<?> constructor, final int i) {
		Assertion.check().isNotNull(constructor);
		//-----
		final String named = getNamedValue(constructor.getParameterAnnotations()[i]);
		final Class<?> rootType = constructor.getParameterTypes()[i];

		isOptional = caseOptional(rootType);
		isList = caseList(rootType);
		type = (isOptional || isList) ? ClassUtil.getGeneric(constructor, i) : rootType;
		targetId = named != null ? named : DIAnnotationUtil.buildId(type);
	}

	/**
	 * @return Inject name
	 */
	String getName() {
		return targetId;
	}

	/**
	 * @return if optional
	 */
	boolean isOptional() {
		return isOptional;
	}

	/**
	 * @return if required (not null)
	 */
	boolean isRequired() {
		return !(isList || isOptional);
	}

	/**
	 * @return is list
	 */
	boolean isList() {
		return isList;
	}

	/**
	 * @return get object class
	 */
	Class<?> getType() {
		return type;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (isList) {
			return targetId + '*';
		} else if (isOptional) {
			return targetId + '?';
		}
		return targetId;
	}

	private static boolean caseList(final Class<?> type) {
		return List.class.isAssignableFrom(type);
	}

	private static boolean caseOptional(final Class<?> type) {
		return Optional.class.isAssignableFrom(type);
	}

	private static String getNamedValue(final Annotation[] annotations) {
		return Arrays.stream(annotations)
				.filter(annotation -> annotation instanceof ParamValue)
				.map(annotation -> ParamValue.class.cast(annotation).value())
				.findFirst().orElse(null);
	}
}
