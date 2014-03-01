package io.vertigo.commons.script;

import io.vertigo.commons.script.parser.ScriptSeparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Type de séparateur.
 * Permet de définir les types de séparateurs utilisés dans les fichiers.
 * 
 * @author  pchretien
 * @version $Id: SeparatorType.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public enum SeparatorType {

	/**
	 * Séparateurs de type XML/HTML.
	 */
	XML("&lt;%", "%&gt;"),
	/**
	 * Séparateur de type text.
	 */
	CLASSIC(SeparatorType.BEGIN_SEPARATOR_CLASSIC, SeparatorType.END_SEPARATOR_CLASSIC),
	/**
	 * Séparateur de code dans du XML.
	 */
	XML_CODE("&lt;#", "#&gt;");

	/**
	 * Début d'une balise d'évaluation classique.
	 */
	public static final String BEGIN_SEPARATOR_CLASSIC = "<%";

	/**
	 * Fin d'une balise d'évaluation classique.
	 */
	public static final String END_SEPARATOR_CLASSIC = "%>";
	private final List<ScriptSeparator> separatorList;

	private SeparatorType(final String startExpression, final String endExpression) {
		separatorList = new ArrayList<>(1);
		separatorList.add(new ScriptSeparator(startExpression, endExpression));
	}

	/**
	 * @return Liste des ScriptSeparator pour ce SeparatorType.
	 */
	public List<ScriptSeparator> getSeparators() {
		return Collections.unmodifiableList(separatorList);
	}
}
