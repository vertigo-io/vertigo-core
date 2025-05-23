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
package io.vertigo.core.lang;

import java.util.Objects;
import java.util.function.Supplier;

import io.vertigo.core.util.StringUtil;

/**
 * Design by Contract implementation through assertions.
 * Provides a fluent API for runtime validation of program invariants.
 * 
 * Features:
 * - Null checks (isNotNull, isNull)
 * - String validation (isNotBlank)
 * - Boolean conditions (isTrue, isFalse)
 * - Conditional assertions (when)
 * - Formatted error messages with parameters
 * 
 * Based on B.Meyer's Design by Contract principles from Eiffel language.
 * Throws specific exceptions when assertions fail.
 *
 * Usage example:
 * Assertion.check()
 *         .isNotNull(object, "Object {0} required", objectName)
 *         .isTrue(value > 0, "Positive value required");
 *
 * @author pchretien, fconstantin
 */
public final class Assertion {
	private static final Assertion INSTANCE = new Assertion();

	private Assertion() {
		super();
	}

	public static Assertion check() {
		return INSTANCE;
	}

	/**
	 * Evaluates an assertion when a condition is fulfilled.
	 *
	 * @param condition the condition to check the assertion
	 * @param assertionSupplier the assertion to check when the condition is fulfilled
	 * @return the current assertion
	 */
	public Assertion when(final boolean condition, final Supplier<Assertion> assertionSupplier) {
		if (condition) {
			isValid(assertionSupplier);
		}
		return INSTANCE;
	}

	/**
	 * Checks if an object is not null.
	 * Throws the famous NullPointerException if not.
	 *
	 * @param o the object
	 * @return the current assertion
	 */
	public Assertion isNotNull(final Object o) {
		Objects.requireNonNull(o);
		return this;
	}

	/**
	 * Checks if an object is not null.
	 * Throws the famous NullPointerException with a pretty message if not.
	 *
	 * @param o the object
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isNotNull(final Object o, final String msg, final Object... params) {
		//Attention si o est un Boolean : il peut s'agir du resultat d'un test (boolean) qui a été autoboxé en Boolean
		Objects.requireNonNull(o, () -> StringUtil.format(msg, params));
		return this;
	}

	/**
	 * Checks if an object is null.
	 * Throws an IllegalStateException if not
	 * @param o the object
	 * @return the current assertion
	 */
	public Assertion isNull(final Object o) {
		return isNull(o, "this object must be null");
	}

	/**
	 * Checks if an object is null.
	 * Throws an illegalStateException with a pretty message if not.
	 *
	 * @param o the object
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isNull(final Object o, final String msg, final Object... params) {
		if (o != null) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
		return this;
	}

	/**
	 * Checks if a boolean expression is True
	 * Throws an IllegalStateException with a pretty message if not.
	 *
	 * @param test the boolean expression
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isTrue(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalStateException(StringUtil.format(msg, params));
		}
		return this;
	}

	/**
	 * Checks if a boolean expression is False
	 * Throws an IllegalStateException with a pretty message if not.
	 *
	 * @param test the boolean expression
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isFalse(final boolean test, final String msg, final Object... params) {
		return isTrue(!test, msg, params);
	}

	/**
	 * Checks if a string is not blank (and not empty).
	 * Throws an IllegalArgumentException if not.
	 *
	 * @param str the string
	 * @return the current assertion
	 */
	public Assertion isNotBlank(final String str) {
		return isNotBlank(str, "String must not be empty");
	}

	/**
	 * Checks if a string is not blank (and not empty).
	 * Throws an IllegalArgumentException with a pretty message if not.
	 *
	 * @param str the string
	 * @param msg the message
	 * @param params the params of the message
	 * @return the current assertion
	 */
	public Assertion isNotBlank(final String str, final String msg, final Object... params) {
		isNotNull(str, msg, params);
		//---
		if (StringUtil.isBlank(str)) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
		return this;
	}

	/**
	 * Checks if an assertion supplied is valid.
	 * Throws the Exception thrown by the assertion if not.
	 *
	 * @param assertionSupplier the assertion supplied
	 * @return the current assertion
	 */
	public Assertion isValid(final Supplier<Assertion> assertionSupplier) {
		assertionSupplier.get();
		return this;
	}
}
