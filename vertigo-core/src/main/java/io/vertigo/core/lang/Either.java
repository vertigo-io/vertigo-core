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

import java.util.Optional;

/**
 * Immutable object representing an Either type.
 * Implements the Either monad pattern to handle two possible types of values.
 * Useful for representing success/failure scenarios or optional transformations.
 * Only one of the two possible types can be present at a time.
 *
 * @author pchretien
 * @param <L> Type of left element
 * @param <R> Type of right element
 */
public record Either<L, R> (Optional<L> left, Optional<R> right) {
	public Either {
		Assertion.check()
				.isNotNull(left)
				.isNotNull(right);
	}

	/**
	 * Creates an Either with a right element.
	 *
	 * @param <L> Type of left element
	 * @param <R> Type of right element
	 * @param right Right element
	 * @return New Either with right element
	 */
	public static <L, R> Either<L, R> right(final R right) {
		return new Either<>(Optional.empty(), Optional.of(right));
	}

	/**
	 * Creates an Either with a left element.
	 *
	 * @param <L> Type of left element
	 * @param <R> Type of right element
	 * @param left Left element
	 * @return New Either with left element
	 */
	public static <L, R> Either<L, R> left(final L left) {
		return new Either<>(Optional.of(left), Optional.empty());
	}
}
