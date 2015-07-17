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
package io.vertigo.core.impl.environment.kernel.model;

import io.vertigo.lang.Assertion;

/**
 * Clé d'une definition.
 * @author  pchretien
 */
public final class DynamicDefinitionKey {
	/**
	 * Nom de la dynamic Definition.
	 */
	private final String name;

	/**
	* Constructeur.
	* @param name Nom de la Définition
	*/
	public DynamicDefinitionKey(final String name) {
		Assertion.checkNotNull(name);
		//-----
		this.name = name;
	}

	/**
	 * @return Nom de la Définition
	 */
	public String getName() {
		return name;
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return getName();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof DynamicDefinitionKey) {
			return ((DynamicDefinitionKey) o).getName().equals(getName());
		}
		return false;
	}
}
