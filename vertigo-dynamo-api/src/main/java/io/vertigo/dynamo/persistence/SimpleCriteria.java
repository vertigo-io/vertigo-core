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
package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;

/**
 * Critère de recherche simple.
 *
 * @author npiedeloup
 * @param <D> Type de l'objet
 */
public final class SimpleCriteria<D extends DtObject> implements Criteria<D> {
	private static final long serialVersionUID = -1279372740797454047L;
	private final String search;

	/**
	 * Constructeur.
	 * @param search recherche simple
	 */
	public SimpleCriteria(final String search) {
		Assertion.checkNotNull(search);
		//---------------------------------------------------------------------
		this.search = search;
	}

	/**
	* Recherche simple.
	* @return Recherche saisie par l'utilisateur.
	*/
	public String getSearch() {
		return search;
	}
}
