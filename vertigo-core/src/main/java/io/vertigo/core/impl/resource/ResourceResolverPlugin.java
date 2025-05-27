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
package io.vertigo.core.impl.resource;

import java.net.URL;

import io.vertigo.core.node.component.Plugin;

/**
 * Resolves a resource by providing its URL.
 * This interface defines a plugin contract for resolving resources represented as strings into their corresponding URL objects.
 * Implementations of this interface are expected to handle the conversion of a resource string into a valid URL, if possible.
 *
 * @author prahmoune
 */
public interface ResourceResolverPlugin extends Plugin {
	/**
	 * Resolves a resource string into its corresponding URL.
	 * This method takes a string representation of a resource (e.g., a file path, URI, or other identifier) and attempts to convert it into a URL object.
	 * If the resource cannot be resolved (e.g., due to an invalid format or inaccessible resource), an empty Optional is returned.
	 *
	 * @param resource the string representation of the resource to resolve
	 * @return The resolved URL if successful, or null if the resource cannot be resolved
	 * @throws NullPointerException if the provided resource string is null
	 */
	URL resolve(String resource);
}
