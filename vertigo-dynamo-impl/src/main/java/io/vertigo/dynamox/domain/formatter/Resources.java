package io.vertigo.dynamox.domain.formatter;

import io.vertigo.kernel.lang.MessageKey;

/**
 * Dictionnaire des ressources.
 *
 * @author  pchretien
 * @version $Id: Resources.java,v 1.2 2013/10/22 11:00:06 pchretien Exp $
*/
public enum Resources implements MessageKey {
	/**
	 * Type de donnée erroné.
	 */
	DYNAMOX_NUMBER_NOT_FORMATTED,

	/**
	 * Le nombre est trop grand.
	 */
	DYNAMOX_NUMBER_TOO_BIG,

	/**
	 * Erreur de formattage d'un booléen.
	 */
	DYNAMOX_BOOLEAN_NOT_FORMATTED,

	/**
	 * La date ne doit pas contenir de lettre.
	 */
	DYNAMOX_DATE_NOT_FORMATTED_LETTER,

	/**
	 * Ce champ doit contenir une date valide.
	 */
	DYNAMOX_DATE_NOT_FORMATTED
}
