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

import java.util.Optional;

/**
 * Either is an immutable object, it contains left XOR right element.
 * 
 * @param <L> Type left
 * @param <R> Type right
 *
 * @author pchretien
 */
public final record Either<L, R> (Optional<L> left, Optional<R> right) {
	public Either {
		Assertion.check()
				.isNotNull(left)
				.isNotNull(right);
	}

	/**
	 * Creates an Either with a right element.
	 * @param <L> the left type element 
	 * @param <R> the right type element 
	 * @param right the right element 
	 * 
	 * the left element is empty.
	 * @return the created Either object
	 */
	public static <L, R> Either<L, R> right(final R right) {
		return new Either<>(Optional.empty(), Optional.of(right));
	}

	/**
	 * Creates an Either with a left element.
	 * @param <L> the left type element 
	 * @param <R> the right type element 
	 * @param left the left element 
	 * @return the created Either object
	 */
	public static <L, R> Either<L, R> left(final L left) {
		return new Either<>(Optional.of(left), Optional.empty());
	}
}
