package io.vertigo.commons.script.parser;

/**
 * Permet de traiter un script pars�.
 * Notification des nouveaux param�tres trouv�s lors du parsing d'un script.
 * - Soit on trouve des expressions entre deux s�parateurs.
 * - Soit on trouve du texte. 
 * 
 * - Les s�parareurs sont d�finis par  
 * - un carat�re (En ce cas le doubler signifie que l'on souhaite conserver le caract�re en tant que texte)
 * - une chaine de d�but et une de fin.
 * 
 * @author  pchretien
 * @version $Id: ScriptParserHandler.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface ScriptParserHandler {
	/**
	 * Ev�nement lors du parsing d'un param�tre compris entre 2 s�parateurs.
	 *
	 * @param expression String Chaine comprise entre les 2 s�parateurs
	 * @param separator S�parateur de d�but
	 */
	void onExpression(String expression, ScriptSeparator separator);

	/**
	 * Ev�nement lors du parsing indique la zone entre deux expressions. 
	 * @param text Texte compris entre les 2 s�parateurs 
	 */
	void onText(String text);
}
