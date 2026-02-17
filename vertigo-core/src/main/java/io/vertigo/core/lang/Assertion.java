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

	/**
	 * Creates a new assertion chain.
	 * Starting point for fluent assertion API.
	 *
	 * @return Assertion instance for chaining
	 */
	public static Assertion check() {
		return INSTANCE;
	}

	/**
	 * Evaluates an assertion when a condition is met.
	 * Allows conditional validation without breaking the chain.
	 *
	 * @param condition Condition to evaluate
	 * @param assertionSupplier Assertion to execute if condition is true
	 * @return Current assertion for chaining
	 */
	public Assertion when(final boolean condition, final Supplier<Assertion> assertionSupplier) {
		if (condition) {
			isValid(assertionSupplier);
		}
		return INSTANCE;
	}

	/**
	 * Validates that an object is not null.
	 * Standard null check without custom message.
	 *
	 * @param o Object to check
	 * @return Current assertion for chaining
	 * @throws NullPointerException if object is null
	 */
	public Assertion isNotNull(final Object o) {
		Objects.requireNonNull(o);
		return this;
	}

	/**
	 * Validates that an object is not null with custom message.
	 * Supports message formatting with parameters.
	 *
	 * @param o Object to check
	 * @param msg Error message template
	 * @param params Message parameters
	 * @return Current assertion for chaining
	 * @throws NullPointerException if object is null
	 */
	public Assertion isNotNull(final Object o, final String msg, final Object... params) {
		//Warning: if o is a Boolean, it may be the result of a test (boolean) that was autoboxed to Boolean
		Objects.requireNonNull(o, () -> StringUtil.format(msg, params));
		return this;
	}

	/**
	 * Validates that an object is null.
	 * Uses default error message.
	 *
	 * @param o Object to check
	 * @return Current assertion for chaining
	 * @throws IllegalArgumentException if object is not null
	 */
	public Assertion isNull(final Object o) {
		return isNull(o, "this object must be null");
	}

	/**
	 * Validates that an object is null with custom message.
	 * Supports message formatting with parameters.
	 *
	 * @param o Object to check
	 * @param msg Error message template
	 * @param params Message parameters
	 * @return Current assertion for chaining
	 * @throws IllegalArgumentException if object is not null
	 */
	public Assertion isNull(final Object o, final String msg, final Object... params) {
		if (o != null) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
		return this;
	}

	/**
	 * Validates that a condition is true.
	 * Supports message formatting with parameters.
	 *
	 * @param test Condition to evaluate
	 * @param msg Error message template
	 * @param params Message parameters
	 * @return Current assertion for chaining
	 * @throws IllegalStateException if condition is false
	 */
	public Assertion isTrue(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalStateException(StringUtil.format(msg, params));
		}
		return this;
	}

	/**
	 * Validates that a condition is false.
	 * Supports message formatting with parameters.
	 *
	 * @param test Condition to evaluate
	 * @param msg Error message template
	 * @param params Message parameters
	 * @return Current assertion for chaining
	 * @throws IllegalStateException if condition is true
	 */
	public Assertion isFalse(final boolean test, final String msg, final Object... params) {
		return isTrue(!test, msg, params);
	}

	/**
	 * Validates that a string is not blank.
	 * Uses default error message.
	 *
	 * @param str String to check
	 * @return Current assertion for chaining
	 * @throws IllegalArgumentException if string is blank
	 */
	public Assertion isNotBlank(final String str) {
		return isNotBlank(str, "String must not be empty");
	}

	/**
	 * Validates that a string is not blank with custom message.
	 * Supports message formatting with parameters.
	 *
	 * @param str String to check
	 * @param msg Error message template
	 * @param params Message parameters
	 * @return Current assertion for chaining
	 * @throws IllegalArgumentException if string is blank
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
	 * Validates a supplied assertion.
	 * Allows composition of multiple assertions.
	 *
	 * @param assertionSupplier Assertion to validate
	 * @return Current assertion for chaining
	 * @throws Exception from supplied assertion if validation fails
	 */
	public Assertion isValid(final Supplier<Assertion> assertionSupplier) {
		assertionSupplier.get();
		return this;
	}
}
