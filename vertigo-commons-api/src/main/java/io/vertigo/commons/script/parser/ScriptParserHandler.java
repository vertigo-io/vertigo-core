package io.vertigo.commons.script.parser;

/**
 * Permet de traiter un script parsé.
 * Notification des nouveaux paramètres trouvés lors du parsing d'un script.
 * - Soit on trouve des expressions entre deux séparateurs.
 * - Soit on trouve du texte. 
 * 
 * - Les séparateurs sont définis par  
 * - un caractère (En ce cas le doubler signifie que l'on souhaite conserver le caractère en tant que texte)
 * - une chaine de début et une de fin.
 * 
 * @author  pchretien
 * @version $Id: ScriptParserHandler.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface ScriptParserHandler {
	/**
	 * Evénement lors du parsing d'un paramètre compris entre 2 séparateurs.
	 *
	 * @param expression String Chaine comprise entre les 2 séparateurs
	 * @param separator Séparateur de début
	 */
	void onExpression(String expression, ScriptSeparator separator);

	/**
	 * Evénement lors du parsing indique la zone entre deux expressions. 
	 * @param text Texte compris entre les 2 séparateurs 
	 */
	void onText(String text);
}