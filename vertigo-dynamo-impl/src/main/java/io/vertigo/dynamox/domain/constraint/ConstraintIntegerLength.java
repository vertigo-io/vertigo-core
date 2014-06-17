package io.vertigo.dynamox.domain.constraint;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

/**
 * Contrainte vérifiant que l'objet est : <ul>
 * <li>soit un Integer comprenant au maximum le nombre de chiffres précisé à la construction (nombres de digits)</li>
 * <li>soit null</li>
 * </ul><br>
 * On rappelle que le maximum d'un type Integer est compris entre 1O^9 et 10^10 <br>
 * On conseille donc d'utiliser 10^9 comme structure de stockage max en BDD : donc number(9) <br>
 * Si vous souhaitez flirter avec les 10^10 alors n'utilisez pas de contraintes.
 *
 * @author pchretien
 */
public final class ConstraintIntegerLength extends AbstractConstraintLength<Integer> {
	/**
	 * Borne maximale au sens strict de Integer (= 10 puissance maxLength)
	 */
	private int maxValue;

	/**
	 * Borne minimale au sens strict de Integer (= - maxValue)
	 */
	private int minValue;

	public ConstraintIntegerLength(final String name) {
		super(name);
	}

	/**
	 * Constructeur nécessaire pour le ksp.
	 * @param args Liste des arguments réduite à un seul castable en integer.
	 * Cet argument correspond au nombre de chifres maximum authorisé sur le Integer.
	 */
	@Override
	public void initParameters(final String args) {
		setMaxLength(args);
		//----------------------------------------------------------------------
		Assertion.checkArgument(getMaxLength() < 10, "Longueur max doit être strictement inférieure à 10");
		//----------------------------------------------------------------------
		int tmpMaxValue = 1;
		tmpMaxValue = 1;
		for (int i = 0; i < getMaxLength(); i++) {
			tmpMaxValue *= 10;
		}
		maxValue = tmpMaxValue;
		minValue = -tmpMaxValue;
	}

	/** {@inheritDoc} */
	public boolean checkConstraint(final Integer value) {
		if (value == null) {
			return true;
		}
		final int i = value.intValue();
		return i > minValue && i < maxValue;
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_INTEGERLENGTH_EXCEEDED, minValue, maxValue);
	}
}
