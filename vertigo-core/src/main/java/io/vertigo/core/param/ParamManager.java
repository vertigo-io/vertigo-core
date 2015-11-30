package io.vertigo.core.param;

import io.vertigo.lang.Manager;

/**
 * Interface du gestionnaire de la configuration applicative.
 *
 * Une configuration possède une liste de paramètres.
 * Un paramètre est
 *  - identifié par un nom.
 *  - camelCase.camelCase et ne contient que des lettres et chiffres; les séparateurs sont des points.
 *
 * Les paramètres sont de trois types :
 * -boolean
 * -String
 * -int
 *
 *
 * Exemple en json :
 *
 * {
 *  server.host : "wiki",
 *  server.port : "5455",
 *  maxUsers  :"10",
 * }
 *
 *
 * getStringValue("server.host") => wiki
 * getStringValue("host") => erreur.
 *
 * @author pchretien, npiedeloup, prahmoune
 */
public interface ParamManager extends Manager {

	<C> C getValue(String paramName, Class<C> paramType);

	/**
	 * Return a param as a String.
	 * @param paramName param's name
	 * @return Value of the param
	 */
	String getStringValue(final String paramName);

	/**
	 * Return a param as an int.
	 * @param paramName param's name
	 * @return Value of the param
	 */
	int getIntValue(String paramName);

	/**
	 * Return a param as a long.
	 * @param paramName param's name
	 * @return Value of the param
	 */
	long getLongValue(final String paramName);

	/**
	 * Return a param as a boolean .
	 * @param paramName param's name
	 * @return Value of the param
	 */
	boolean getBooleanValue(String paramName);
}
