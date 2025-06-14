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

import java.util.HashMap;
import java.util.Map;

/**
 * Fluent builder for Map construction.
 * Provides type-safe map creation with various put operations and validation:
 * - Required values (put)
 * - Optional values (putNullable)
 * - Duplicate key check (putCheckKeyNotExists)
 * - Bulk insertion (putAll)
 * Can create immutable maps using unmodifiable().
 *
 * @author pchretien
 * @param <K> Type of keys
 * @param <V> Type of mapped values
 */
public final class MapBuilder<K, V> implements Builder<Map<K, V>> {
	private final Map<K, V> myMap = new HashMap<>();
	private boolean unmodifiable;

	/**
	 * Adds a key-value pair with duplicate key check.
	 * Ensures no existing value for the key.
	 *
	 * @param key Key to add
	 * @param value Non-null value to associate
	 * @return Current builder for chaining
	 * @throws IllegalArgumentException if key already exists
	 */
	public MapBuilder<K, V> putCheckKeyNotExists(final K key, final V value) {
		Assertion.check()
				.isNotNull(key)
				.isNotNull(value);
		//-----
		final Object previous = myMap.put(key, value);
		Assertion.check()
				.isNull(previous, "Data with key '{0}' already registered", key);
		return this;
	}

	/**
	 * Adds all entries from another map.
	 * All values must be non-null.
	 *
	 * @param map Source map to copy from
	 * @return Current builder for chaining
	 */
	public MapBuilder<K, V> putAll(final Map<K, V> map) {
		Assertion.check()
				.isNotNull(map, "map cannot be null");
		//-----
		map.forEach(this::put);
		return this;
	}

	/**
	 * Adds a required key-value pair.
	 * Both key and value must be non-null.
	 *
	 * @param key Key to add
	 * @param value Non-null value to associate
	 * @return Current builder for chaining
	 */
	public MapBuilder<K, V> put(final K key, final V value) {
		Assertion.check()
				.isNotNull(key, "key cannot be null")
				.isNotNull(value, "value cannot be null");
		//-----
		myMap.put(key, value);
		return this;
	}

	/**
	 * Adds an optional key-value pair.
	 * Only adds if value is non-null.
	 *
	 * @param key Key to add
	 * @param value Value to associate (may be null)
	 * @return Current builder for chaining
	 */
	public MapBuilder<K, V> putNullable(final K key, final V value) {
		Assertion.check()
				.isNotNull(key, "key cannot be null");
		//-----
		if (value != null) {
			myMap.put(key, value);
		}
		return this;
	}

	/**
	 * Makes the resulting map unmodifiable.
	 * Creates defensive copy on build.
	 *
	 * @return Current builder for chaining
	 */
	public MapBuilder<K, V> unmodifiable() {
		unmodifiable = true;
		return this;
	}

	/**
	 * Creates the final map.
	 * Returns unmodifiable copy if requested.
	 *
	 * @return Built map instance
	 */
	@Override
	public Map<K, V> build() {
		return unmodifiable ? Map.copyOf(myMap) : myMap;
	}
}
