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
package io.vertigo.lang;

import java.util.NoSuchElementException;

/**
 * This class, inspired by scala, allows you to define optional types.
 * An option is used to design a nullable type.
 *  An option is empty or defined with a content.
 *  - none : empty
 *  - some : defined with a content
 *
 * @author jmainaud
 * @param <T> the type of the option.
 */
public final class Option<T> {
	/** There is only one none option. */
	private static final Option<Object> NONE = new Option<>(null);

	/** Value. */
	private final T value;

	/**
	 * Constructor.
	 * Creates an option with a value that can be null.
	 * @param value the value
	 */
	private Option(final T value) {
		this.value = value;
	}

	/**
	 * Provides an empty option.
	 *
	 * @param <T> Type de l'option demandé.
	 * @return None.
	 */
	public static <T> Option<T> empty() {
		return (Option<T>) NONE;
	}

	/**
	 * Provides a defined option.
	 *
	 * @param <T> the type of the option
	 * @param value the value of the option
	 * @return the option.
	 */
	public static <T> Option<T> of(final T value) {
		Assertion.checkNotNull(value, "Option.some requires a non null value.");
		//-----
		return new Option<>(value);
	}

	/**
	 * Provides an option(empty or defined) depending on the value.
	 *
	 * @param <T> the type of the option
	 * @param value the value of the option
	 * @return the option.
	 */
	public static <T> Option<T> ofNullable(final T value) {
		if (value == null) {
			return empty();
		}
		return of(value);
	}

	/**
	 * Returns true if the option is defined, aka not empty.
	 * An option is defined if it has a value.
	 *
	 * @return if the option is defined.
	 */
	public boolean isPresent() {
		return value != null;
	}

	/**
	 * Returns true if the option is undefined, aka empty.
	 * An option is defined if it has a value.
	 *
	 * @return if the option is undefined.
	 * @deprecated Should use !isPresent() like Jdk8, may be remove in next release
	 */
	@Deprecated
	public boolean iEmpty() {
		return !isPresent();
	}

	/**
	 * Returns the content of the option.
	 * If the option is empty then an exception is thrown.
	 *
	 * @return the content of the option
	 * @throws NoSuchElementException if the option is empty.
	 */
	public T get() {
		if (value == null) {
			throw new NoSuchElementException();
		}
		return value;
	}

	/**
	 * If the option is defined returns the value, ellse return the default value.
	 *
	 * @param defaut the default value
	 * @return the value if defined else the default value
	 */
	public T orElse(final T defaut) {
		return value == null ? defaut : value;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return value == null ? "" : value.toString();

	}
}
