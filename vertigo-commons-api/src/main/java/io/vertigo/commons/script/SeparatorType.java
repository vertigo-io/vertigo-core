package io.vertigo.commons.script;

import io.vertigo.commons.script.parser.ScriptSeparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Type de s�parateur.
 * Permet de d�finir les types de s�parateurs utilis�s dans les fichiers.
 * 
 * @author  pchretien
 * @version $Id: SeparatorType.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public enum SeparatorType {

	/**
	 * S�parateurs de type XML/HTML.
	 */
	XML("&lt;%", "%&gt;"),
	/**
	 * S�parateur de type text.
	 */
	CLASSIC(SeparatorType.BEGIN_SEPARATOR_CLASSIC, SeparatorType.END_SEPARATOR_CLASSIC),
	/**
	 * S�parateur de code dans du XML.
	 */
	XML_CODE("&lt;#", "#&gt;");

	/**
	 * D�but d'une balise d'�valuation classique.
	 */
	public static final String BEGIN_SEPARATOR_CLASSIC = "<%";

	/**
	 * Fin d'une balise d'�valuation classique.
	 */
	public static final String END_SEPARATOR_CLASSIC = "%>";
	private final List<ScriptSeparator> separatorList;

	private SeparatorType(final String startExpression, final String endExpression) {
		separatorList = new ArrayList<ScriptSeparator>(1);
		separatorList.add(new ScriptSeparator(startExpression, endExpression));
	}

	/**
	 * @return Liste des ScriptSeparator pour ce SeparatorType.
	 */
	public List<ScriptSeparator> getSeparators() {
		return Collections.unmodifiableList(separatorList);
	}
}
