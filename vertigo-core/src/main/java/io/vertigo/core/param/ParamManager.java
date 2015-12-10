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
	/**
	 * Returns the value for a param, defined by its name.
	 * @param paramName Name of the param
	 * @param paramType Type of the param
	 * @return the value of the param
	 */
	<C> C getValue(String paramName, Class<C> paramType);

	/**
	 * Returns a param as a String.
	 * @param paramName param's name
	 * @return the value of the param
	 */
	String getStringValue(final String paramName);

	/**
	 * Returns a param as an int.
	 * @param paramName Name of the param
	 * @return the value of the param
	 */
	int getIntValue(String paramName);

	/**
	 * Returns a param as a long.
	 * @param paramName Name of the param
	 * @return the value of the param
	 */
	long getLongValue(final String paramName);

	/**
	 * Returns a param as a boolean .
	 * @param paramName Name of the param
	 * @return the value of the param
	 */
	boolean getBooleanValue(String paramName);
}
