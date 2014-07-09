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
package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.kernel.lang.Assertion;

/**
 * Identifiant d'un objet powerAMC.
 *
 * @author pchretien
 */
final class OOMId {
	private final String keyValue;

	/**
	 * Constructeur.
	 * @param keyValue Valeur de l'identiant
	 */
	OOMId(final String keyValue) {
		Assertion.checkNotNull(keyValue);
		//------------------------------------------------------------------
		this.keyValue = keyValue;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return keyValue.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof OOMId) {
			return ((OOMId) o).keyValue.equals(this.keyValue);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "id(" + keyValue + ')';
	}
}
