package io.vertigo.dynamo.impl.collections.functions.filter;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.kernel.lang.Assertion;

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
