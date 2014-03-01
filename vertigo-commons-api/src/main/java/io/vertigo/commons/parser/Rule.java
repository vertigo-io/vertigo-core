package io.vertigo.commons.parser;

/**
 * Règle.
 *
 * @author pchretien
 * @version $Id: Rule.java,v 1.4 2013/08/02 09:26:28 pchretien Exp $
 */
public interface Rule<R> {
	/**
	 * @return Expression de la règle.
	 */
	String getExpression();

	Parser<R> createParser();
}
