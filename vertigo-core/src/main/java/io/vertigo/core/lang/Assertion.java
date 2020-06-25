/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.lang;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import io.vertigo.core.util.StringUtil;

/**
 * Assertions have been introduced by  B.Meyer with a language called Eiffel.
 *
 * An assertion allows you to design by contract.
 * Each time an assetion fails, an specific exception is thrown.
 * - checkNotNull     throws NullPointerException
 * - checkArgument    throws IllegalArgumentException
 * - checkArgNotEmpty throws IllegalArgumentException
 * - checkState       throws IllegalStateException
 *
 * Assertion can define a message and args.
 * "hello {0}, an error occured on '{1}'", "foo", "bar"
 *  hello foo, an error occured on 'bar'
 *
 * You can use ' inside the message.
 *
 *
 * @author fconstantin
 */
public final class Assertion {
	private final boolean enabled;

	private Assertion(final boolean enabled) {
		this.enabled = enabled;
	}

	private static final Assertion ENABLED = new Assertion(true);
	private static final Assertion DISABLED = new Assertion(false);

	/**
	 * @param ifCondition condition of this assertion
	 * @return Assertion to check if condition is true
	 */
	public static Assertion when(final boolean ifCondition) {
		return ifCondition ? ENABLED : DISABLED;
	}

	public static Assertion check() {
		return ENABLED;
	}

	public Assertion isNotNull(final Object o) {
		if (enabled) {
			Objects.requireNonNull(o);
		}
		return this;
	}

	public Assertion isNotNull(final Object o, final String msg, final Object... params) {
		if (enabled) {
			//Attention si o est un Boolean : il peut s'agir du resultat d'un test (boolean) qui a été autoboxé en Boolean
			Objects.requireNonNull(o, () -> StringUtil.format(msg, params));
		}
		return this;
	}

	public Assertion argument(final boolean test, final String msg, final Object... params) {
		if (enabled) {
			if (!test) {
				throw new IllegalArgumentException(StringUtil.format(msg, params));
			}
		}
		return this;
	}

	public Assertion isTrue(final BooleanSupplier test, final String msg, final Object... params) {
		if (enabled) {
			isTrue(test.getAsBoolean(), msg, params);
		}
		return this;
	}

	public Assertion isFalse(final BooleanSupplier test, final String msg, final Object... params) {
		if (enabled) {
			isTrue(!test.getAsBoolean(), msg, params);
		}
		return this;
	}

	public Assertion isTrue(final boolean test, final String msg, final Object... params) {
		if (enabled) {
			if (!test) {
				throw new IllegalStateException(StringUtil.format(msg, params));
			}
		}
		return this;
	}

	public Assertion isFalse(final boolean test, final String msg, final Object... params) {
		return isTrue(!test, msg, params);
	}

	public Assertion isNotBlank(final String str) {
		return isNotBlank(str, "String must not be empty");
	}

	public Assertion isNotBlank(final String str, final String msg, final Object... params) {
		if (enabled) {
			isNotNull(str, msg, params);
			if (StringUtil.isBlank(str)) {
				throw new IllegalArgumentException(StringUtil.format(msg, params));
			}
		}
		return this;
	}

}
