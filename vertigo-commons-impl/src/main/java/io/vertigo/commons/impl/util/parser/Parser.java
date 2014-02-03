package io.vertigo.commons.impl.util.parser;

/**
 * R�gle.
 * Si elle est respect�e l'index augmente sinon une erreur est d�clench�e.
 *
 * @author pchretien
 * @version $Id: Parser.java,v 1.1 2013/07/29 09:44:01 pchretien Exp $
 */
public interface Parser<P> {
	/**
	 * Retourne le prochain num�ro de ligne
	 * Le pattern est OK du num�ro de ligne pass� en param�tre au num�ro de ligne retourn�.
	 * @param text Texte � parser
	 * @param start D�but du parsing
	 * @throws NotFoundException Si la r�gle n'est pas applicable.
	 * @return Index de fin
	 */
	int parse(String text, int start) throws NotFoundException;

	P get();
}
