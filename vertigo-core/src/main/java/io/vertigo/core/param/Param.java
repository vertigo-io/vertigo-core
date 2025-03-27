/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.param;

import java.util.regex.Pattern;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;

/**
 * Represents a configuration parameter.
 *
 * This class allows the creation of configuration parameters, where each
 * parameter is identified by a name and has a corresponding value. The name of
 * a parameter must match a specified pattern, and the value can be of type
 * String, Integer, or boolean.
 *
 * @author: pchretien
 */
public final class Param {
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	/** Regular expression for paramName. */
	private static final Pattern REGEX_PARAM_NAME = Pattern
			.compile("([a-zA-Z][a-zA-Z0-9]*)([\\._-][a-zA-Z][a-zA-Z0-9]*){0,200}");
	private final String name;
	private final String value;

	/**
	 * Constructor.
	 *
	 * @param name  the name of the parameter
	 * @param value the value of the parameter
	 */
	private Param(final String name, final String value) {
		Assertion.check().isNotBlank(name).isTrue(REGEX_PARAM_NAME.matcher(name).matches(),
				"param '{0}' must match pattern {1}", name, REGEX_PARAM_NAME).isNotBlank(name).isNotNull(value);
		// ---
		this.name = name;
		this.value = value;

	}

	/**
	 * Creates a new Integer parameter.
	 *
	 * @param name  the name of the parameter
	 * @param value the value of the parameter
	 * @return a new parameter
	 */
	public static Param of(final String name, final Integer value) {
		return new Param(name, Integer.toString(value));
	}

	/**
	 * Creates a new parameter.
	 *
	 * @param name  the name of the parameter
	 * @param value the value of the parameter
	 * @return a new parameter
	 */
	public static Param of(final String name, final String value) {
		return new Param(name, value);
	}

	/**
	 * Gets the value of the parameter.
	 *
	 * @return the value of the parameter
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the name of the parameter.
	 *
	 * @return the name of the parameter
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parameter as a typed value.
	 *
	 * @param paramType the type of the parameter
	 * @return the value of the parameter
	 */
	public <O> O getValue(final Class<O> paramType) {
		Assertion.check().isNotNull(paramType);
		// -----
		return (O) parse(name, paramType, value);
	}

	/**
	 * Returns the parameter as a String.
	 *
	 * @return the value of the parameter
	 */
	public String getValueAsString() {
		return getValue(String.class);
	}

	/**
	 * Returns the parameter as an int.
	 *
	 * @return the value of the parameter
	 */
	public int getValueAsInt() {
		return getValue(int.class);
	}

	/**
	 * Returns the parameter as a long.
	 *
	 * @return the value of the parameter
	 */
	public long getValueAsLong() {
		return getValue(long.class);
	}

	/**
	 * Returns the parameter as a boolean.
	 *
	 * @return the value of the parameter
	 */
	public boolean getValueAsBoolean() {
		return getValue(boolean.class);
	}

	private static Object parse(final String paramName, final Class<?> paramType, final String paramValue) {
		Assertion.check().isNotNull(paramName).isNotNull(paramType).isNotNull(paramValue);
		// -----
		try {
			if (String.class.equals(paramType)) {
				return paramValue;
			} else if (Boolean.class.equals(paramType) || boolean.class.equals(paramType)) {
				return toBoolean(paramName, paramValue);
			} else if (Integer.class.equals(paramType) || int.class.equals(paramType)) {
				return Integer.valueOf(paramValue);
			} else if (Long.class.equals(paramType) || long.class.equals(paramType)) {
				return Long.valueOf(paramValue);
			} else {
				throw new IllegalArgumentException("type '" + paramType + "' unsupported");
			}
		} catch (final Exception e) {
			throw new VSystemException(e, "Param :{0} with value :{1} can't be cast into '{2}'", paramName, paramValue,
					paramType);
		}
	}

	private static boolean toBoolean(final String paramName, final String paramValue) {
		if (!(TRUE.equalsIgnoreCase(paramValue) || FALSE.equalsIgnoreCase(paramValue))) {
			throw new VSystemException("Param :{0} with value :{1} can't be cast into 'boolean'", paramName,
					paramValue);
		}
		return Boolean.parseBoolean(paramValue);
	}

}
