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
package io.vertigo.core.node.component.di;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.vertigo.core.lang.Assertion;

/**
 * Reactor.
 * - adds components in any order with their id, their class
 * - adds ids for components that are already existing.
 * - then 'proceed' and you obtain an ORDERED list of components, taking account of their dependencies.
 *
 * @author pchretien
 */
public final class DIReactor {
	//Map of components and their parents
	private final Set<String> allComponentInfos = new HashSet<>();
	private final List<DIComponentInfo> diComponentInfos = new ArrayList<>();
	private final Set<String> parentComponentInfos = new HashSet<>();

	private void check(final String id) {
		//The id must be unique
		if (allComponentInfos.contains(id)) {
			throw new DIException("Two components with the same id :'" + id + "'");
		}
	}

	/**
	 * Adds a component
	 * @param id ID f the component
	 * @param implClass Impl class of the component
	 * @return this reactor
	 */
	public DIReactor addComponent(final String id, final Class<?> implClass) {
		Assertion.check()
				.isNotBlank(id)
				.isNotNull(implClass);
		//---
		return addComponent(id, implClass, Collections.emptySet());
	}

	/**
	 *
	 * Adds a component
	 * @param id ID f the component
	 * @param implClass Impl class of the component
	 * @param params List of ID of all local params - which will be automatically injected-
	 * @return this reactor
	 */
	public DIReactor addComponent(final String id, final Class<?> implClass, final Set<String> params) {
		Assertion.check()
				.isNotBlank(id)
				.isNotNull(implClass)
				.isNotNull(params);
		//---
		final DIComponentInfo diComponentInfo = new DIComponentInfo(id, implClass, params);
		check(diComponentInfo.id());
		allComponentInfos.add(diComponentInfo.id());
		diComponentInfos.add(diComponentInfo);
		return this;
	}

	/**
	 * Adds a component identified by its ID.
	 * This component is ready to be injected in other components (and it does not need to be resolved).
	 * @param id ID of the component
	 * @return this reactor
	 */
	public DIReactor addParent(final String id) {
		Assertion.check().isNotBlank(id);
		//---
		check(id);
		allComponentInfos.add(id);
		parentComponentInfos.add(id);
		return this;
	}

	/**
	 * Process the 'digital' reaction in a way to obtain an ordered list of components, taking account of their dependencies.
	 * @return Ordered list of component's Ids.
	 */
	public List<String> proceed() {
		//-----
		//1. Check that all components defined by their ids exist
		final StringBuilder missing = new StringBuilder();
		for (final DIComponentInfo componentInfo : diComponentInfos) {
			for (final DIDependency dependency : componentInfo.dependencies()) {
				//If a reference is required
				//and it is absent, then it is missing!
				if (dependency.isRequired() && !allComponentInfos.contains(dependency.getName())) {
					missing.append(dependency).append(" (referenced by ").append(componentInfo).append("), ");
				}
			}
		}
		if (missing.length() > 0) {
			throw new DIException("Components or params not found :" + missing.toString() + "\n\tLoaded components/params : " + diComponentInfos);
		}
		//-----
		//2. Resolve dependencies
		final List<DIComponentInfo> unsorted = new ArrayList<>(diComponentInfos);
		//Dependency levels of components
		final List<String> sorted = new ArrayList<>();

		//By default, all parents are considered already sorted.
		//We will sort the new components.
		while (!unsorted.isEmpty()) {
			final int countSorted = sorted.size();
			for (final Iterator<DIComponentInfo> iterator = unsorted.iterator(); iterator.hasNext();) {
				final DIComponentInfo componentInfo = iterator.next();
				final boolean solved = isSolved(componentInfo, parentComponentInfos, allComponentInfos, sorted);
				if (solved) {
					//The component is resolved
					// - Add its key to the list of resolved component keys
					// - Remove it from the list of components to resolve
					sorted.add(componentInfo.id());
					iterator.remove();
				}
			}
			// If no progress was made during an iteration, there is a cyclic dependency
			if (countSorted == sorted.size()) {
				// Cyclic dependency detected!
				throw new DIException("Dependencies can't be solved on components (maybe a cyclic dependency) :" + unsorted);
			}
		}
		//-----
		//3. Expose a list of ids, not the componentInfos
		return Collections.unmodifiableList(sorted);

	}

	private static boolean isSolved(final DIComponentInfo componentInfo, final Set<String> parentComponentInfos, final Set<String> allComponentInfos, final List<String> sorted) {
		//A component is resolved when
		// all required dependencies are already resolved
		// and all optional dependencies are resolved (if present)
		for (final DIDependency dependency : componentInfo.dependencies()) {
			if (!isSolved(dependency, parentComponentInfos, allComponentInfos, sorted)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSolved(final DIDependency dependency, final Set<String> parentComponentInfos, final Set<String> allComponentInfos, final List<String> sorted) {
		//A dependency is resolved when all related ids are resolved.
		//If the dependency is already resolved, move on to the next one.

		if (dependency.isRequired()) {
			return parentComponentInfos.contains(dependency.getName()) || sorted.contains(dependency.getName());
		} else if (dependency.isOptional()) {
			//If the object is in the list, then it must be resolved.
			if (allComponentInfos.contains(dependency.getName())) {
				return sorted.contains(dependency.getName()) || parentComponentInfos.contains(dependency.getName());
			}
			//Otherwise, since it is optional, it's ok.
			return true;
		} else if (dependency.isList()) {
			//If the object is in the list, then it must be resolved.
			for (final String id : allComponentInfos) {
				final boolean match = id.equals(dependency.getName()) || id.startsWith(dependency.getName() + '#');
				if (match && !sorted.contains(id) && !parentComponentInfos.contains(id)) {
					//The object id is part of the list
					return false;
				}
			}
			return true;
		}
		throw new IllegalStateException();
	}
}
