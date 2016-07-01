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
package io.vertigo.dynamo.impl.collections.functions.filter;

import java.io.Serializable;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

/**
 * Filtre sur champ=valeur.
 *
 * @param <D> Type du DtObject
 * @param <C> Type du champs filtré
 */
public final class DtListRangeFilter<D extends DtObject, C extends Comparable> implements DtListFilter<D>, Serializable {
	private static final long serialVersionUID = 3469510250178487305L;
	/** Nom du champ. */
	private final String fieldName;

	private final C minValue;
	private final C maxValue;
	private final boolean isMinInclude;
	private final boolean isMaxInclude;

	/** Champ concerné. */
	private transient DtField dtField;

	/**
	 * Constructeur.
	 * @param fieldName Nom du champ
	 * @param minValue Valeur min
	 * @param maxValue Valeur max
	 * @param isMinInclude Si valeur min incluse
	 * @param isMaxInclude Si valeur max incluse
	 *
	 */
	public DtListRangeFilter(final String fieldName, final Option<C> minValue, final Option<C> maxValue, final boolean isMinInclude, final boolean isMaxInclude) {
		Assertion.checkArgNotEmpty(fieldName);
		Assertion.checkNotNull(minValue);
		Assertion.checkNotNull(maxValue);
		//-----
		this.fieldName = fieldName;
		this.minValue = minValue.orElse(null); //On remet a null (car Option non serializable)
		this.maxValue = maxValue.orElse(null); //On remet a null (car Option non serializable)
		this.isMinInclude = isMinInclude;
		this.isMaxInclude = isMaxInclude;

		//-----
		// On vérifie le caractère serializable, car il est difficile de gérer cette propriété par les generics de bout en bout
		if (this.minValue != null) {
			Assertion.checkArgument(this.minValue instanceof Serializable, "Les valeurs doivent être Serializable (min:{0})", this.minValue.getClass().getSimpleName());
		}
		if (this.maxValue != null) {
			Assertion.checkArgument(this.maxValue instanceof Serializable, "Les valeurs doivent être Serializable (max:{0})", this.maxValue.getClass().getSimpleName());
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final D dto) {
		getDtField(dto);
		return accept(dtField.getDataAccessor().getValue(dto));
	}

	private void getDtField(final D dto) {
		if (dtField == null) {
			final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
			dtField = dtDefinition.getField(fieldName);
		}
	}

	private boolean accept(final Object value) {
		if (value == null) {
			return false; //objet null toujours hors range
		}
		Assertion.checkArgument(value instanceof Comparable, "La valeur doit être Comparable : {0}.", value.getClass().getName());
		final Comparable comparableValue = Comparable.class.cast(value);
		final int minValueCompare = minValue != null ? comparableValue.compareTo(minValue) : 1; //si empty=>* : toujours ok
		final int maxValueCompare = maxValue != null ? comparableValue.compareTo(maxValue) : -1; //si empty=>* : toujours ok
		return (isMinInclude ? minValueCompare >= 0 : minValueCompare > 0) //supérieur (ou egale) au min
				&& (isMaxInclude ? maxValueCompare <= 0 : maxValueCompare < 0); //inférieur (ou egale) au max
	}
}
