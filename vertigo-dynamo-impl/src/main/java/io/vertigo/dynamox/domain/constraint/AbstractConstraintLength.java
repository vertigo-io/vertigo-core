package io.vertigo.dynamox.domain.constraint;

import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractConstraintImpl;
import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation de base des contraintes de longueur sur une donnée quelconque.
 * -numérique,
 * -textuelle.
 *
 * @author pchretien
 * @param <D> Type java de la valeur à contréler
 */
abstract class AbstractConstraintLength<D> extends AbstractConstraintImpl<Integer, D> {
	/**
	 * Nombre maximum de caractères pour une chaine, de chiffres pour un entier...
	 */
	private int maxLength;

	protected AbstractConstraintLength(final String name) {
		super(name);
	}

	/**
	* @param max Nombre maximum de caractères, de chiffres...
	*/
	protected final void setMaxLength(final String max) {
		maxLength = Integer.parseInt(max);
		//----------------------------------------------------------------------
		Assertion.checkArgument(maxLength > 0, "Longueur max doit être strictement positive");
	}

	/**
	 * @return int Nombre maximum de caractères, de chiffres...
	 */
	public final int getMaxLength() {
		return maxLength;
	}

	/** {@inheritDoc} */
	public final Property getProperty() {
		return DtProperty.MAX_LENGTH;
	}

	/** {@inheritDoc} */
	public final Integer getPropertyValue() {
		return getMaxLength();
	}
}
