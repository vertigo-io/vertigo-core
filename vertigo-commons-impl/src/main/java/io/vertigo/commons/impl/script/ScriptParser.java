package io.vertigo.commons.impl.script;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

/**
 * Parse une chaine de caractères. 
 * (script SQL ou autre)
 * Notifie le handler des paramètres trouvés.
 *
 * @author  pchretien
 */
final class ScriptParser {
	/**
	 * Liste des séparateurs admis pour cet analyseur.
	 */
	private final List<ScriptSeparator> separators;

	/**
	 * Index de début du paramètre courant
	 * On commence au début.
	 * (-1 pour éviter de squizzer le premier caractère)
	 */
	private int currentBeginCar = -1;

	/**
	 * Type (du séparateur) de paramètre courant
	 */
	private ScriptSeparator currentSeparator;

	/**
	 * Constructeur.
	 * @param separators Séparateurs pris en compte
	 */
	ScriptParser(final List<ScriptSeparator> separators) {
		Assertion.checkNotNull(separators);
		//----------------------------------------------------------------------
		this.separators = separators;
	}

	/**
	 * Parse le script, notifie le handler.
	 * La grammaire est constituées de simples balises (Séparateurs). 
	 *
	 * @param script Script à analyser
	 * @param scriptHandler Handler gérant la grammaire analysée
	 */
	void parse(final String script, final ScriptParserHandler scriptHandler) {
		Assertion.checkNotNull(script);
		Assertion.checkNotNull(scriptHandler);
		//----------------------------------------------------------------------
		int index = 0;
		int endCar = -1;

		// On isole les chaines entre 2 séparateurs
		while (nextPosition(script, endCar + 1)) {
			endCar = currentSeparator.indexOfEndCaracter(script, currentBeginCar + 1);
			//Il faut qu'il y ait le caractère de fin correspondant.
			if (endCar == -1) {
				throw new IllegalStateException("Aucun séparateur de fin trouvé pour " + currentSeparator + " à la position " + currentBeginCar + " sur " + script);
			}

			onRequestText(scriptHandler, script, index, currentBeginCar);
			//Si le séparateur est un char, sa longueur est de 1.
			if (currentSeparator.isCar()) {
				onRequestParam(scriptHandler, script, currentBeginCar + 1, endCar, currentSeparator);
				index = endCar + 1;
			} else {
				//Si le séparateur est une chaine de caractères, sa longueur doit être calculée.
				onRequestParam(scriptHandler, script, currentBeginCar + currentSeparator.getBeginSeparator().length(), endCar, currentSeparator);
				index = endCar + currentSeparator.getEndSeparator().length();
			}
		}
		onRequestText(scriptHandler, script, index, script.length());
	}

	private static void onRequestParam(final ScriptParserHandler scriptHandler, final String script, final int beginCar, final int endCar, final ScriptSeparator separator) {
		if (endCar == beginCar) {
			if (separator.isCar()) {
				//Si il s'agissait de deux mêmes caractères collés,
				//c'est que l'on voulait le caractère lui même. (Echappement du séparateur)
				//on le remet donc dans la requete
				scriptHandler.onText(String.valueOf(separator.getSeparator()));
			} else {
				throw new VRuntimeException("Le paramètre est vide");
			}
		} else {
			scriptHandler.onExpression(script.substring(beginCar, endCar), separator);
		}
	}

	private static void onRequestText(final ScriptParserHandler scriptHandler, final String script, final int beginCar, final int endCar) {
		scriptHandler.onText(script.substring(beginCar, endCar));
	}

	/**
	 * Recherche dans une requête la position de la prochaine occurence
	 * d'un paramètre.
	 * @param script Script
	 * @param beginCar Index de début de recherche
	 *
	 * @return Si il existe une prochaine position trouvée.
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
		//Si il existe un séparateur
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
