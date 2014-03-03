package io.vertigo.dynamox.task;

import io.vertigo.kernel.lang.Assertion;

/**
 * Ce processor permet de supprimer les retours chariots en trop dans les requêtes sql dynamiques.
 * @author npiedeloup
 * @version $Id: TrimPreProcessor.java,v 1.1 2014/01/15 09:44:53 npiedeloup Exp $
 */
final class TrimPreProcessor {
	private final String beginSeparator;
	private final String endSeparator;

	/**
	 * Constructeur.
	 * @param beginSeparator marqueur de début
	 * @param endSeparator marqueur de fin
	 */
	TrimPreProcessor(final String beginSeparator, final String endSeparator) {
		Assertion.checkArgNotEmpty(beginSeparator);
		Assertion.checkArgNotEmpty(endSeparator);
		//---------------------------------------------------------------------
		this.beginSeparator = beginSeparator;
		this.endSeparator = endSeparator;
	}

	/**
	 * Effectue un nettoyage de la requete SQL.
	 * @param sqlQuery Chaine à parser
	 * @return Chaine processée
	 */
	String evaluate(final String sqlQuery) {
		final StringBuilder sb = new StringBuilder(sqlQuery.length());
		int index = 0;
		int beginIndex = sqlQuery.indexOf(beginSeparator, index);
		while (beginIndex != -1) {
			sb.append(sqlQuery.substring(index, beginIndex).trim()).append(' ');
			index = sqlQuery.indexOf(endSeparator, beginIndex) + endSeparator.length();
			sb.append(sqlQuery.substring(beginIndex, index)).append(' ');
			beginIndex = sqlQuery.indexOf(beginSeparator, index);
		}
		sb.append(sqlQuery.substring(index).trim());
		return sb.toString();
	}
}
