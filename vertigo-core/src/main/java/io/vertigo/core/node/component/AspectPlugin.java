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
package io.vertigo.core.node.component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.vertigo.core.node.component.aspect.Aspect;

/**
 * An interface for creating and managing proxy references for components with Aspect-Oriented Programming (AOP) support.
 *
 * This plugin enables the creation of proxy references that implement aspects, allowing for the injection of cross-cutting
 * concerns (e.g., logging, security, transaction management) into a component's lifecycle. 
 * It extends the Plugin interface to integrate with the plugin ecosystem.
 * 
 * @author pchretien
 */
public interface AspectPlugin extends Plugin {

	/**
	 * Creates a proxy reference for a component instance, applying the specified aspects at defined join points.
	 *
	 * The proxy wraps the provided component instance, intercepting method calls as defined by the join points and their
	 * associated aspects. This enables the implementation of cross-cutting concerns such as logging, security, or
	 * performance monitoring.
	 *
	 * @param <C> the type of the component, which must extend CoreComponent
	 * @param instance  the component instance to be wrapped by the proxy
	 * @param joinPoints a map associating methods to lists of Aspect objects defining the interception logic
	 * @return a proxy reference implementing the same interface as the provided component instance	 
	 */
	<C extends CoreComponent> C wrap(final C instance, Map<Method, List<Aspect>> joinPoints);

	/**
	 * Retrieves the original component instance from a proxy reference.
	 *
	 * This method unwraps the proxy to return the underlying component instance, 
	 * removing any aspect-related interception logic. 
	 * If the provided component is not a proxy, the original instance is returned unchanged.
	 *
	 * @param <C>      the type of the component, which must extend CoreComponent
	 * @param component the proxy or component instance to unwrap
	 * @return the underlying component instance
	 */
	<C extends CoreComponent> C unwrap(final C component);
}
