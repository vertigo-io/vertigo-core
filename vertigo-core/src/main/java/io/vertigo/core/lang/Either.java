/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2024, Vertigo.io, team@vertigo.io
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
 * Immutable object representing an Either, containing a left or a right element.
 *
 * An Either is an immutable object that represents a choice between two types of elements: left or right.
 *
 * @author pchretien
 *
 * @param <L> the type of the left element
 * @param <R> the type of the right element
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
     * @param <L>   the type of the left element
     * @param <R>   the type of the right element
     * @param right the right element
     * @return the created Either object with the right element
     */
	public static <L, R> Either<L, R> right(final R right) {
		return new Either<>(Optional.empty(), Optional.of(right));
	}

	/**
     * Creates an Either with a left element.
     *
     * @param <L>  the type of the left element
     * @param <R>  the type of the right element
     * @param left the left element
     * @return the created Either object with the left element
     */
	public static <L, R> Either<L, R> left(final L left) {
		return new Either<>(Optional.of(left), Optional.empty());
	}
}
