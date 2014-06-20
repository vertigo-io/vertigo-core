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
package io.vertigo.kernel.resource;

import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Centralisation des accès aux composants et aux plugins d'un module.
 * Les composants sont d'un type M.
 * @author pchretien
 */
public final class ResourceSpace implements Activeable {
	private final List<ResourceLoader> resourceLoaders = new ArrayList<>();

	public void start() {
		//
	}

	public void stop() {
		resourceLoaders.clear();
	}

	public List<ResourceLoader> getResourceLoaders() {
		return Collections.unmodifiableList(resourceLoaders);
	}

	public void addLoader(ResourceLoader resourceLoader) {
		Assertion.checkNotNull(resourceLoader);
		//---------------------------------------------------------------------
		resourceLoaders.add(resourceLoader);
	}
}
