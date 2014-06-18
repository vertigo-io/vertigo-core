package io.vertigo.dynamox.domain.constraint;

import io.vertigo.kernel.lang.MessageText;

/**
 * Contrainte pour gérer la longueur des chaines de caractères.
 *
 * @author  plepaisant
 */
public final class ConstraintStringLength extends AbstractConstraintLength<String> {
	public ConstraintStringLength(final String name) {
		super(name);
	}

	/**
	 * @param args Liste des arguments réduite à un seul castable en integer.
	 * Cet argument correspond au nombre de caractères maximum authorisés sur la chaine de caractères.
	 */
	@Override
	public void initParameters(final String args) {
		setMaxLength(args);
	}

	/** {@inheritDoc} */
	public boolean checkConstraint(final String value) {
		if (value == null) {
			return true;
		}
		return value.length() <= getMaxLength();
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_STRINGLENGTH_EXCEEDED, Integer.toString(getMaxLength()));
		//return "la taille doit être inférieure à " + maxLength + " caractères.";
	}
}
