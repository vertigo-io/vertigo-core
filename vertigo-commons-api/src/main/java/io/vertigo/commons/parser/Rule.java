package io.vertigo.commons.parser;

/**
 * Règle.
 *
 * @author pchretien
 */
public interface Rule<R> {
	/**
	 * @return Expression de la règle.
	 */
	String getExpression();

	Parser<R> createParser();
}
