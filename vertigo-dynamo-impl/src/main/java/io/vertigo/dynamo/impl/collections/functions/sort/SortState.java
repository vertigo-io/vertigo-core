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
package io.vertigo.dynamo.impl.collections.functions.sort;

import io.vertigo.lang.Assertion;

/**
 * Gestion des ordres de tri.
 *
 * @author pchretien
 */
public final class SortState {
	/** Nom de la Colonne de tri. */
	private final String fieldName;
	/** Gestion du tri insensible à la casse. Valable uniquement pour les String. */
	private final boolean ignoreCase;
	/** Gestion de l'ordre de tri. */
	private final boolean nullLast;
	/** Indique le sens du tri a effectuer. */
	private final boolean desc;

	/**
	 * @param fieldName Nom du champ concerné par le tri
	 * @param desc Si tri descendant
	 * @param nullLast Si les objets Null sont en derniers
	 * @param ignoreCase Si on ignore la casse
	 */
	public SortState(final String fieldName, final boolean desc, final boolean nullLast, final boolean ignoreCase) {
		Assertion.checkArgNotEmpty(fieldName);
		//-----
		this.fieldName = fieldName;
		this.desc = desc;
		this.nullLast = nullLast;
		this.ignoreCase = ignoreCase;
	}

	/**
	 * @return Nom du champ à trier
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @return Si le tri est descendant (sinon ascendant)
	 */
	public boolean isDesc() {
		return desc;
	}

	/**
	 * @return Si le tri ignore la casse
	 */
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	/**
	 * @return Si le tri positionne les valeurs null à la fin
	 */
	public boolean isNullLast() {
		return nullLast;
	}
}
