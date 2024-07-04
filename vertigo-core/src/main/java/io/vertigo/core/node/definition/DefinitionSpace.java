/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.definition;

import java.util.Set;

/**
 * DefinitionSpace represents a space for managing and resolving definitions.
 * It is not thread-safe.
 * Definitions within this space can be checked, resolved, and retrieved based on their names and types.
 *
 * @author mlaroche
 */
public interface DefinitionSpace {

	/**
	 * Checks if this container contains a definition with the specified name.
	 *
	 * @param name the name of the definition to check
	 * @return true if the definition is already registered, false otherwise
	 */
	boolean contains(String name);

	/**
	 * Resolves a definition from its name and class type.
	 *
	 * @param <D>   Type of the definition
	 * @param name  the name of the definition to resolve
	 * @param clazz the class type of the definition
	 * @return the resolved definition
	 */
	<D extends Definition> D resolve(String name, Class<D> clazz);

	/**
	 * Gets a list of all types of definitions managed within this space.
	 *
	 * @return a set of classes representing the types of definitions
	 */
	Set<Class<? extends Definition>> getAllTypes();

	/**
	 * Gets an ordered set of all objects for a specified definition type.
	 *
	 * @param <C>   Type of the definition
	 * @param clazz the class type of the definition
	 * @return an ordered set of all objects for the specified definition type
	 */
	<C extends Definition> Set<C> getAll(Class<C> clazz);
}
