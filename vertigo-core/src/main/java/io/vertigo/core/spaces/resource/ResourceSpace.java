/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.core.spaces.resource;

import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralisation des accès aux composants et aux plugins d'un module.
 * Les composants sont d'un type M.
 * @author pchretien
 */
public final class ResourceSpace implements Activeable {
	private final Map<String, ResourceLoader> resourceLoaders = new HashMap<>();

	public void start() {
		//
	}

	public void stop() {
		resourceLoaders.clear();
	}

	//	public ResourceLoader getResourceLoader(String type) {
	//		Assertion.checkArgNotEmpty(type);
	//		Assertion.checkArgument(resourceLoaders.containsKey(type), "this type {0} of resource is not yet defined", type);
	//		//---------------------------------------------------------------------
	//		return resourceLoaders.get(type);
	//	}

	public Collection<ResourceLoader> getResourceLoaders() {
		return Collections.unmodifiableCollection(resourceLoaders.values());
	}

	public void addLoader(ResourceLoader resourceLoader) {
		Assertion.checkNotNull(resourceLoader);
		Assertion.checkArgument(!resourceLoader.getTypes().isEmpty(), "a loader must be able to parse at least one type of resource");
		//---------------------------------------------------------------------
		for (String type : resourceLoader.getTypes()) {
			ResourceLoader previous = resourceLoaders.put(type, resourceLoader);
			Assertion.checkArgument(previous == null, "this type {0} of resource is already defined", type);
		}
	}
}
