package io.vertigo.dynamo.impl.collections.functions.filter;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.io.Serializable;

/**
 * Filtre sur champ=valeur.
 *
 * @version $Id: DtListRangeFilter.java,v 1.6 2014/01/20 17:45:43 pchretien Exp $
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
		Assertion.checkNotNull(isMinInclude);
		Assertion.checkNotNull(isMaxInclude);
		//---------------------------------------------------------------------
		this.fieldName = fieldName;
		this.minValue = minValue.getOrElse(null); //On remet a null (car Option non serializable)
		this.maxValue = maxValue.getOrElse(null); //On remet a null (car Option non serializable)
		this.isMinInclude = isMinInclude;
		this.isMaxInclude = isMaxInclude;

		//---------------------------------------------------------------------
		// On vérifie le caractère serializable, car il est difficile de gérer cette propriété par les generics de bout en bout
		if (this.minValue != null) {
			Assertion.checkArgument(this.minValue instanceof Serializable, "Les valeurs doivent être Serializable (min:{0})", this.minValue.getClass().getSimpleName());
		}
		if (this.maxValue != null) {
			Assertion.checkArgument(this.maxValue instanceof Serializable, "Les valeurs doivent être Serializable (max:{0})", this.maxValue.getClass().getSimpleName());
		}
	}

	/** {@inheritDoc} */
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
