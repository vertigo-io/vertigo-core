package io.vertigo.dynamox.domain.constraint;

import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractConstraintImpl;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

/**
 * Contrainte sur la valeur minimale d'un nombre.
 * arguments = valeur minimale.
 * @author npiedeloup
 */
public final class ConstraintNumberMinimum extends AbstractConstraintImpl<Number, Number> {
	private double minValue;

	public ConstraintNumberMinimum(final String urn) {
		super(urn);
	}

	/**{@inheritDoc}*/
	@Override
	public void initParameters(final String args) {
		Assertion.checkArgument(args != null && args.length() > 0, "Vous devez préciser la valeur minimum comme argument de ConstraintNumberMinimum");
		//---------------------------------------------------------------------
		minValue = Double.valueOf(args);
	}

	/** {@inheritDoc} */
	public boolean checkConstraint(final Number value) {
		if (value == null) {
			return true;
		}
		return value.doubleValue() >= minValue;
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_NUMBER_MINIMUM, minValue);
	}

	/** {@inheritDoc} */
	public Property getProperty() {
		return DtProperty.MIN_VALUE;
	}

	/** {@inheritDoc} */
	public Number getPropertyValue() {
		return minValue;
	}
}
