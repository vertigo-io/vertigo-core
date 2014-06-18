package io.vertigo.dynamox.domain.constraint;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

/**
 * Contrainte vérifiant que l'objet est : <ul>
 * <li>soit un Long comprenant au maximum le nombre de chiffres précisé à la construction (nombres de digits)</li>
 * <li>soit null</li>
 * </ul><br>
 * On rappelle que le maximum d'un type Long est compris entre 1O^18 et 10^19 <br>
 * On conseille donc d'utiliser 10^18 comme structure de stockage max en BDD : donc number(18) <br>
 * Si vous souhaitez flirter avec les 10^19 alors n'utilisez pas de contraintes.
 *
 * @author pchretien
 */
public final class ConstraintLongLength extends AbstractConstraintLength<Long> {
	/**
	 * Borne maximale au sens strict de Long (= 10 puissance maxLength)
	 */
	private long maxValue;

	/**
	 * Borne minimale au sens strict de Long (= - maxValue)
	 */
	private long minValue;

	public ConstraintLongLength(final String name) {
		super(name);
	}

	/**
	 * @param args Liste des arguments réduite à un seul castable en long.
	 * Cet argument correspond au nombre de chifres maximum authorisé sur le Long.
	 */
	@Override
	public void initParameters(final String args) {
		setMaxLength(args);
		//----------------------------------------------------------------------
		Assertion.checkArgument(getMaxLength() < 19, "Longueur max doit être strictement inférieure à 19");
		//----------------------------------------------------------------------
		long tmpMaxValue = 1;
		for (int i = 0; i < getMaxLength(); i++) {
			tmpMaxValue *= 10;
		}
		maxValue = tmpMaxValue;
		minValue = -tmpMaxValue;
	}

	/** {@inheritDoc} */
	public boolean checkConstraint(final Long value) {
		if (value == null) {
			return true;
		}
		final long i = value.longValue();
		return i > minValue && i < maxValue;
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_LONGLENGTH_EXCEEDED, minValue, maxValue);
	}
}
