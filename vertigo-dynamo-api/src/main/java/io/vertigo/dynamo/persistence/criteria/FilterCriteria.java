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
package io.vertigo.dynamo.persistence.criteria;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.persistence.Criteria;
import io.vertigo.lang.Assertion;

import java.util.Collections;
import java.util.Map;

/**
 * Critère de recherche par champs.
 * Les champs de l'objet recherché sont filtrés par la valeur associée au champ.
 * - Soit de type égalité.
 * - Soit de type préfixe (Commence par).
 *
 * @author npiedeloup
 * @param <D> Type de l'objet
 */
public final class FilterCriteria<D extends DtObject> implements Criteria<D> {
	private static final long serialVersionUID = -4980252957531667077L;
	private final Map<String, Object> mapFilter;
	private final Map<String, String> mapPrefix;

	/**
	 * Constructeur.
	 * @param mapFilter Liste des filtrages
	 * @param mapPrefix Liste des prefixes
	 */
	FilterCriteria(final Map<String, Object> mapFilter, final Map<String, String> mapPrefix) {
		Assertion.checkNotNull(mapFilter);
		Assertion.checkNotNull(mapPrefix);
		//-----
		this.mapFilter = Collections.unmodifiableMap(mapFilter);
		this.mapPrefix = Collections.unmodifiableMap(mapPrefix);
	}

	/**
	* Critère de recherche par champs.
	* @return Map des filtres existant.
	*/
	public Map<String, Object> getFilterMap() {
		return mapFilter;
	}

	/**
	* Critère de prefix par champs.
	* @return Map des prefixes existant.
	*/
	public Map<String, String> getPrefixMap() {
		return mapPrefix;
	}

	/**
	 * @return Si le groupe est vide
	 */
	public boolean isEmpty() {
		return mapFilter.isEmpty() && mapPrefix.isEmpty();
	}
}
