package io.vertigo.commons.parser;

/**
 * Règle.
 * Si elle est respectée l'index augmente sinon une erreur est déclenchée.
 *
 * @author pchretien
 * @version $Id: Parser.java,v 1.1 2013/07/29 09:44:01 pchretien Exp $
 */
public interface Parser<P> {
	/**
	 * Retourne le prochain numéro de ligne
	 * Le pattern est OK du numéro de ligne passé en paramètre au numéro de ligne retourné.
	 * @param text Texte à parser
	 * @param start Début du parsing
	 * @throws NotFoundException Si la règle n'est pas applicable.
	 * @return Index de fin
	 */
	int parse(String text, int start) throws NotFoundException;

	P get();
}
