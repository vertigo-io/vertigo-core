package io.vertigo.commonsimpl.script;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

/**
 * Parse une chaine de caract�res. 
 * (script SQL ou autre)
 * Notifie le handler des param�tres trouv�s.
 *
 * @author  pchretien
 * @version $Id: ScriptParser.java,v 1.4 2013/10/22 12:26:59 pchretien Exp $
 */
final class ScriptParser {
	/**
	 * Liste des s�parateurs admis pour cet analyseur.
	 */
	private final List<ScriptSeparator> separators;

	/**
	 * Index de d�but du param�tre courant
	 * On commence au d�but.
	 * (-1 pour �viter de squizzer le premier caract�re)
	 */
	private int currentBeginCar = -1;

	/**
	 * Type (du s�parateur) de param�tre courant
	 */
	private ScriptSeparator currentSeparator;

	/**
	 * Constructeur.
	 * @param separators S�parateurs pris en compte
	 */
	ScriptParser(final List<ScriptSeparator> separators) {
		Assertion.checkNotNull(separators);
		//----------------------------------------------------------------------
		this.separators = separators;
	}

	/**
	 * Parse le script, notifie le handler.
	 * La grammaire est constitu�es de simples balises (S�parateurs). 
	 *
	 * @param script Script � analyser
	 * @param scriptHandler Handler g�rant la grammaire analys�e
	 */
	void parse(final String script, final ScriptParserHandler scriptHandler) {
		Assertion.checkNotNull(script);
		Assertion.checkNotNull(scriptHandler);
		//----------------------------------------------------------------------
		int index = 0;
		int endCar = -1;

		// On isole les chaines entre 2 s�parateurs
		while (nextPosition(script, endCar + 1)) {
			endCar = currentSeparator.indexOfEndCaracter(script, currentBeginCar + 1);
			//Il faut qu'il y ait le caract�re de fin correspondant.
			if (endCar == -1) {
				throw new IllegalStateException("Aucun s�parateur de fin trouv� pour " + currentSeparator + " � la position " + currentBeginCar + " sur " + script);
			}

			onRequestText(scriptHandler, script, index, currentBeginCar);
			//Si le s�parateur est un char, sa longueur est de 1.
			if (currentSeparator.isCar()) {
				onRequestParam(scriptHandler, script, currentBeginCar + 1, endCar, currentSeparator);
				index = endCar + 1;
			} else {
				//Si le s�parateur est une chaine de caract�res, sa longueur doit �tre calcul�e.
				onRequestParam(scriptHandler, script, currentBeginCar + currentSeparator.getBeginSeparator().length(), endCar, currentSeparator);
				index = endCar + currentSeparator.getEndSeparator().length();
			}
		}
		onRequestText(scriptHandler, script, index, script.length());
	}

	private static void onRequestParam(final ScriptParserHandler scriptHandler, final String script, final int beginCar, final int endCar, final ScriptSeparator separator) {
		if (endCar == beginCar) {
			if (separator.isCar()) {
				//Si il s'agissait de deux m�mes caract�res coll�s,
				//c'est que l'on voulait le caract�re lui m�me. (Echappement du s�parateur)
				//on le remet donc dans la requete
				scriptHandler.onText(String.valueOf(separator.getSeparator()));
			} else {
				throw new VRuntimeException("Le param�tre est vide");
			}
		} else {
			scriptHandler.onExpression(script.substring(beginCar, endCar), separator);
		}
	}

	private static void onRequestText(final ScriptParserHandler scriptHandler, final String script, final int beginCar, final int endCar) {
		scriptHandler.onText(script.substring(beginCar, endCar));
	}

	/**
	 * Recherche dans une requ�te la position de la prochaine occurence
	 * d'un param�tre.
	 * @param script Script
	 * @param beginCar Index de d�but de recherche
	 *
	 * @return Si il existe une prochaine position trouv�e.
	 */
	private boolean nextPosition(final String script, final int beginCar) {
		int minPosition = Integer.MAX_VALUE;
		int position;
		ScriptSeparator found = null;
		for (final ScriptSeparator separator : separators) {
			position = separator.indexOfBeginCaracter(script, beginCar);
			if (position != -1 && position < minPosition) {
				minPosition = position;
				found = separator;
			}
		}
		//Si il existe un s�parateur
		//alors on retourne sa position.
		if (found == null) {
			currentSeparator = null;
			currentBeginCar = -1;
			return false;
		}
		currentSeparator = found;
		currentBeginCar = minPosition;
		return true;

	}
}
