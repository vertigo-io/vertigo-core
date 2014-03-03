package io.vertigo.dynamox.domain.constraint;

import io.vertigo.kernel.lang.MessageText;

import java.math.BigDecimal;

/**
 * Contrainte vérifiant que l'objet est : <ul>
 * <li>soit un BigDecimal comprit dans le segment ]-10^n, 10^n[</li>
 * <li>soit null </li>
 * </ul><br>.
 *
 * @author pchretien
 * @version $Id: ConstraintBigDecimalLength.java,v 1.2 2013/10/22 12:04:44 pchretien Exp $
 */
public final class ConstraintBigDecimalLength extends AbstractConstraintLength<BigDecimal> {
	/**
	 * Borne maximale au sens strict de BigDecimal (= 10 puissance maxLength)
	 */
	private BigDecimal maxValue;

	/**
	 * Borne minimale au sens strict de BigDecimal (= - maxValue)
	 */
	private BigDecimal minValue;

	public ConstraintBigDecimalLength(final String name) {
		super(name);
	}

	/**
	 * Constructeur nécessaire pour le ksp.
	 * @param args Liste des arguments réduite à un seul castable en integer.
	 * Cet argument correspond au nombre de chifres maximum authorisé sur le BigDecimal.
	 * maxLength Valeur n du segment ]-10^n, 10^n[ dans lequel est comprise la valeur.
	 */
	@Override
	public void initParameters(final String args) {
		setMaxLength(args);
		//----------------------------------------------------------------------
		maxValue = BigDecimal.valueOf(1L).movePointRight(getMaxLength());
		minValue = maxValue.negate();
	}

	/** {@inheritDoc} */
	public boolean checkConstraint(final BigDecimal value) {
		if (value == null) {
			return true;
		}
		return value.compareTo(maxValue) < 0 && value.compareTo(minValue) > 0;
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_DECIMALLENGTH_EXCEEDED, minValue, maxValue);
	}
}
