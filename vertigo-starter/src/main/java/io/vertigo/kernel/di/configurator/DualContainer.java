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
package io.vertigo.kernel.di.configurator;

import io.vertigo.kernel.component.Container;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Super Conteneur.
 * 
 * @author pchretien
 * @version $Id: DualContainer.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
final class DualContainer implements Container {
	private final Container container1, container2;
	private final Set<String> ids;

	DualContainer(final Container container1, final Container container2) {
		Assertion.checkNotNull(container1);
		Assertion.checkNotNull(container2);
		//---------------------------------------------------------------------
		this.container1 = container1;
		this.container2 = container2;
		ids = new LinkedHashSet<>();
		ids.addAll(container1.keySet());
		ids.addAll(container2.keySet());
		Assertion.checkArgument(ids.size() == container1.keySet().size() + container2.keySet().size(), "Ambiguit� : il y a des ids en doublon");
	}

	/** {@inheritDoc} */
	public boolean contains(final String id) {
		Assertion.checkNotNull(id);
		// ---------------------------------------------------------------------
		return ids.contains(id);
	}

	/** {@inheritDoc} */
	public <O> O resolve(final String id, final Class<O> clazz) {
		Assertion.checkNotNull(id);
		Assertion.checkNotNull(clazz);
		// ---------------------------------------------------------------------
		if (container1.contains(id)) {
			return container1.resolve(id, clazz);
		}
		if (container2.contains(id)) {
			return container2.resolve(id, clazz);
		}
		throw new VRuntimeException("component info with id '" + id + "' not found.");
	}

	/** {@inheritDoc} */
	public Set<String> keySet() {
		return ids;
	}
}
