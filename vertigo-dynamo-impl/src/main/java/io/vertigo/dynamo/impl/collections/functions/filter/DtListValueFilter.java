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
package io.vertigo.dynamo.impl.collections.functions.filter;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

import java.io.Serializable;

/**
 * Filtre sur champ=valeur.
 *
 * @param <D> Type du DtObject
 */
public final class DtListValueFilter<D extends DtObject> implements DtListFilter<D>, Serializable {
	private static final long serialVersionUID = 7859001120297608977L;

	/** Nom du champ. */
	private final String fieldName;

	/** Valeur à comparer. */
	private final Serializable value;

	/** Champ concerné. */
	private transient DtField dtField;

	/**
	 * Constructeur champ=valeur.
	 * @param fieldName Nom du champ
	 * @param value Valeur
	 */
	public DtListValueFilter(final String fieldName, final Serializable value) {
		Assertion.checkNotNull(fieldName);
		//----------------------------------------------------------------------
		this.fieldName = fieldName;
		this.value = value;
	}

	/** {@inheritDoc} */
	public boolean accept(final D dto) {
		if (dtField == null) {
			final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
			dtField = dtDefinition.getField(fieldName);
		}
		return accept(dtField.getDataAccessor().getValue(dto));
	}

	/**
	 * Détermine si la valeur considérée doit être acceptée dans la sous-liste.
	 * @param fieldValue Valeur du champ
	 * @return Si acceptée
	 */
	private boolean accept(final Object fieldValue) {
		return dtField.getDomain().getDataType().equals(value, fieldValue);
	}
}
