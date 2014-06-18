package io.vertigo.dynamox.domain.constraint;

import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractConstraintImpl;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Manage BigDecimal's constraints.
 * The configuration is like the configuration of Database's decimal (DECIMAL(M,D)).
 * Where M is the maximum of digits (the precision) and D is the number of digits to the right of the decimal point (the scale).
 * @author  mlaroche
 */
public final class ConstraintBigDecimal extends AbstractConstraintImpl<String, BigDecimal> {

	private static final String SEPARATOR_ARGS = ",";
	private Integer maxPrecision;
	private Integer maxScale;

	/**
	 * Constructeur.
	 * @param urn urn
	 */
	public ConstraintBigDecimal(final String urn) {
		super(urn);
	}

	/**
	 * Initialise les paramètres.
	 * @param args args but no args
	 */
	@Override
	public void initParameters(final String args) {
		final String[] beforeAfter = args.split(SEPARATOR_ARGS);
		Assertion.checkState(beforeAfter.length == 2, "L'argument doit être au format M,D. M le nombre de chiffre au total (precision) et D le nombre de chiffre à droite de la virgule (scale).");
		try {
			maxPrecision = Integer.parseInt(beforeAfter[0]);
		} catch (final NumberFormatException e) {
			throw new VRuntimeException("Le nombre de chiffre n'est pas un entier");
		}
		try {
			maxScale = Integer.parseInt(beforeAfter[1]);
		} catch (final NumberFormatException e) {
			throw new VRuntimeException("Le nombre de chiffre après la virgule n'est pas un entier");
		}
		// ---
		Assertion.checkNotNull(maxPrecision, "Le nombre de chiffre ne peut pas être null");
		Assertion.checkNotNull(maxScale, "Le nombre de chiffre après la virgule ne peut pas être null");
		Assertion.checkArgument(maxScale <= maxPrecision, "Le nombre de chiffre après la virgule doit être inférieur au nombre de chiffre total");
	}

	/** {@inheritDoc} */
	@Override
	public boolean checkConstraint(final BigDecimal value) {
		if (value == null) {
			return true;
		}
		final BigDecimal noZero = value.stripTrailingZeros();
		final int scale = noZero.scale();
		final int precision = noZero.precision();
		if (scale > maxScale || precision > maxPrecision) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_DECIMAL_EXCEEDED, new BigDecimal(new BigInteger("1"), 0 - maxPrecision - maxScale), maxScale, maxPrecision - maxScale);
	}

	/** {@inheritDoc} */
	@Override
	public Property<String> getProperty() {
		return new Property<>("numberFormat", String.class);
	}

	/** {@inheritDoc} */
	@Override
	public String getPropertyValue() {
		final StringBuilder numberFormat = new StringBuilder()//
				.append(maxPrecision.toString())//
				.append(SEPARATOR_ARGS)//
				.append(maxScale.toString());
		return numberFormat.toString();
	}

}
