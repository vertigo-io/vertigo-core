package io.vertigo.dynamox.domain.constraint;

import io.vertigo.kernel.lang.MessageKey;

/**
 * Dictionnaire des ressources.
 *
 * @author  pchretien
*/
public enum Resources implements MessageKey {
	/**
	 * Contrainte de longueur pour une String.
	 */
	DYNAMO_CONSTRAINT_STRINGLENGTH_EXCEEDED,

	/**
	 * Contrainte de longueur pour un Integer.
	 */
	DYNAMO_CONSTRAINT_INTEGERLENGTH_EXCEEDED,

	/**
	 * Contrainte de longueur pour un Long.
	 */
	DYNAMO_CONSTRAINT_LONGLENGTH_EXCEEDED,

	/**
	 * Contrainte de longueur pour un BigDecimal.
	 */
	DYNAMO_CONSTRAINT_DECIMALLENGTH_EXCEEDED,

	/**
	 * Contrainte le format d'un BigDecimal.
	 */
	DYNAMO_CONSTRAINT_DECIMAL_EXCEEDED,

	/**
	 * Contrainte de valeur minimum pour un Number.
	 */
	DYNAMO_CONSTRAINT_NUMBER_MINIMUM,

	/**
	 * Contrainte de valeur minimum pour un Number.
	 */
	DYNAMO_CONSTRAINT_NUMBER_MAXIMUM,

	/**
	 * Contrainte par expression regulière.
	 */
	DYNAMO_CONSTRAINT_REGEXP,
}
