/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2021, Vertigo.io, team@vertigo.io
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

/**
 * Tuples are immutable objects.
 * Tuple with 2 Objects.
 * @param <A> Type one
 * @param <B> Type two
 *
 * @author pchretien
 */
public record Tuple<A, B> (A val1, B val2) {
	/**
	 * Creates a Tuple with 2 objects
	 * @param val1 the first object
	 * @param val2 the second object
	 * @return the new tuple created
	 */
	public static <A, B> Tuple<A, B> of(final A val1, final B val2) {
		return new Tuple<>(val1, val2);
	}
}
