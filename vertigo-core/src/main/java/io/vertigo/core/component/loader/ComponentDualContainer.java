/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.component.loader;

import java.util.LinkedHashSet;
import java.util.Set;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;
import io.vertigo.lang.VSystemException;

/**
 * Super Conteneur.
 *
 * @author pchretien
 */
final class ComponentDualContainer implements Container {
	private final Container componentContainer1;
	private final Container componentContainer2;
	private final Set<String> ids;

	ComponentDualContainer(final Container componentContainer1, final Container componentContainer2) {
		Assertion.checkNotNull(componentContainer1);
		Assertion.checkNotNull(componentContainer2);
		//-----
		this.componentContainer1 = componentContainer1;
		this.componentContainer2 = componentContainer2;
		ids = new LinkedHashSet<>();
		ids.addAll(componentContainer1.keySet());
		ids.addAll(componentContainer2.keySet());
		Assertion.checkArgument(ids.size() == componentContainer1.keySet().size() + componentContainer2.keySet().size(), "Ambiguité : il y a des ids en doublon");
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		Assertion.checkNotNull(id);
		//-----
		return ids.contains(id);
	}

	/** {@inheritDoc} */
	@Override
	public <O> O resolve(final String id, final Class<O> clazz) {
		Assertion.checkNotNull(id);
		Assertion.checkNotNull(clazz);
		//-----
		if (componentContainer1.contains(id)) {
			return componentContainer1.resolve(id, clazz);
		}
		if (componentContainer2.contains(id)) {
			return componentContainer2.resolve(id, clazz);
		}
		throw new VSystemException("component info with id '{0}' not found.", id);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return ids;
	}
}
