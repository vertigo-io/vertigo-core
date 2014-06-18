package io.vertigo.dynamox.domain.constraint;

import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractConstraintImpl;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

/**
 * Contrainte sur la valeur maximale d'un nombre.
 * arguments = valeur maximale.
 * @author npiedeloup
 */
public final class ConstraintNumberMaximum extends AbstractConstraintImpl<Number, Number> {
	private double maxValue;

	public ConstraintNumberMaximum(final String urn) {
		super(urn);
	}

	/**{@inheritDoc}*/
	@Override
	public void initParameters(final String args) {
		Assertion.checkArgument(args != null && args.length() > 0, "Vous devez préciser la valeur maximum comme argument de ConstraintNumberMaximum");
		//---------------------------------------------------------------------
		maxValue = Double.valueOf(args);
	}

	/** {@inheritDoc} */
	public boolean checkConstraint(final Number value) {
		if (value == null) {
			return true;
		}
		return value.doubleValue() <= maxValue;
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_NUMBER_MAXIMUM, maxValue);
	}

	/** {@inheritDoc} */
	public Property getProperty() {
		return DtProperty.MAX_VALUE;
	}

	/** {@inheritDoc} */
	public Number getPropertyValue() {
		return maxValue;
	}
}
