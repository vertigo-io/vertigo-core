/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Fluent builder for List construction.
 * Features:
 * - Type-safe list creation
 * - Required value validation
 * - Collection bulk insertion
 * - Optional sorting
 * - Immutability support
 *
 * @author pchretien
 * @param <X> Element type in the list
 */
public final class ListBuilder<X> implements Builder<List<X>> {
	private final List<X> list = new ArrayList<>();
	private boolean unmodifiable;
	private Comparator<? super X> myComparator;

	/**
	 * Adds a required element to the list.
	 * Element must be non-null.
	 *
	 * @param value Element to add
	 * @return Current builder for chaining
	 * @throws NullPointerException if value is null
	 */
	public ListBuilder<X> add(final X value) {
		Assertion.check()
				.isNotNull(value, "Value cannot be null");
		//---
		list.add(value);
		return this;
	}

	/**
	 * Adds all elements from a collection.
	 * All elements must be non-null.
	 *
	 * @param values Collection to add from
	 * @return Current builder for chaining
	 * @throws NullPointerException if collection or any element is null
	 */
	public ListBuilder<X> addAll(final Collection<? extends X> values) {
		Assertion.check()
				.isNotNull(values, "Collection cannot be null");
		//---
		values.forEach(this::add);
		return this;
	}

	/**
	 * Makes the resulting list unmodifiable.
	 * Creates defensive copy on build.
	 *
	 * @return Current builder for chaining
	 */
	public ListBuilder<X> unmodifiable() {
		this.unmodifiable = true;
		return this;
	}

	/**
	 * Sets list sorting comparator.
	 * Applied during build.
	 *
	 * @param comparator Comparator to sort elements
	 * @return Current builder for chaining
	 * @throws IllegalStateException if comparator already set
	 */
	public ListBuilder<X> sort(final Comparator<? super X> comparator) {
		Assertion.check()
				.isNotNull(comparator)
				.isNull(myComparator, "comparator already set");
		//---
		myComparator = comparator;
		return this;
	}

	/**
	 * Creates the final list.
	 * Applies sorting if configured and returns unmodifiable copy if requested.
	 *
	 * @return Built list instance
	 */
	@Override
	public List<X> build() {
		if (myComparator != null) {
			list.sort(myComparator);
		}
		return unmodifiable ? List.copyOf(list) : list;
	}
}
