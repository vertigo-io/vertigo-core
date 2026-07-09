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

/**
 * Builder interface for step by step object construction.
 * Implements the Builder pattern to create complex objects progressively.
 * Particularly useful for creating immutable objects with many optional parameters.
 * Provides a fluent API for better readability and maintainability.
 *
 * @author pchretien
 * @param <T> Type of object to build
 */
public interface Builder<T> {
	/**
	 * Builds and returns the final object.
	 * 
	 * @return Built object
	 */
	T build();
}
