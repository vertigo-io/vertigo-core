package io.vertigo.commons.script.parser;

import io.vertigo.kernel.lang.Assertion;

/**
 * Gestion des S�parateurs utilis�s par le parser.
 * 
 * Un s�parateur est d�fini
 * - soit par un caract�re. (le m�me en d�but et fin)
 *      Exemple #  : #name#
 * - soit par des chaines de caract�res.(qui peuvent �tre diff�rentes)
 *      Exemple <% et %> : XXXX<%if (1=1){%>
 *
 * @author  pchretien
 * @version $Id: ScriptSeparator.java,v 1.4 2013/10/22 12:37:18 pchretien Exp $
 */
public final class ScriptSeparator {
	/**
	 * Le param�tre est-il d�fini par un simple s�parateur.
	 */
	private final boolean isCar;

	/**
	 * Si le param�tre est d�limit� par une String.
	 * On distingue un s�parateur de d�but et un autre de fin
	 */
	private final String beginSeparator;

	/**
	 * S�parateur de fin (String).
	 */
	private final String endSeparator;

	/**
	 * Si le param�tre est d�limit� par un char.
	 */
	private final char separatorCar;

	/**
	 * Constructeur
	 * Si le s�parateur de d�but et de fin sont identiques sous forme de char.
	 * @param separator S�parateur de d�but et de fin
	 */
	public ScriptSeparator(final char separator) {
		isCar = true;
		separatorCar = separator;
		beginSeparator = null;
		endSeparator = null;
	}

	/**
	 * Constructeur
	 * Si le s�parateur de d�but et de fin sont diff�rents sous forme de String.
	 *
	 * @param beginSeparator S�parateur de d�but
	 * @param endSeparator S�parateur de fin
	 */
	public ScriptSeparator(final String beginSeparator, final String endSeparator) {
		Assertion.checkArgNotEmpty(beginSeparator);
		Assertion.checkArgNotEmpty(endSeparator);
		//---------------------------------------------------------------------
		isCar = false;
		separatorCar = ' ';
		this.beginSeparator = beginSeparator;
		this.endSeparator = endSeparator;
	}

	public String getBeginSeparator() {
		Assertion.checkArgument(!isCar, "type de s�parateur inconsistant");
		//---------------------------------------------------------------------
		return beginSeparator;
	}

	public String getEndSeparator() {
		Assertion.checkArgument(!isCar, "type de s�parateur inconsistant");
		//---------------------------------------------------------------------
		return endSeparator;
	}

	/**
	 * @return Caract�re de s�paration, i le param�tre est d�limit� par un char.
	 */
	public char getSeparator() {
		Assertion.checkArgument(isCar, "type de s�parateur inconsistant");
		//---------------------------------------------------------------------
		return separatorCar;
	}

	/**
	 * @return Si le s�parateur est un simple caract�re
	 */
	public boolean isCar() {
		return isCar;
	}

	public int indexOfEndCaracter(final String script, final int start) {
		if (isCar) {
			return script.indexOf(separatorCar, start);
		}
		return script.indexOf(endSeparator, start);

	}

	public int indexOfBeginCaracter(final String script, final int start) {
		if (isCar) {
			return script.indexOf(separatorCar, start);
		}
		return script.indexOf(beginSeparator, start);

	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (isCar) {
			return String.valueOf(separatorCar);
		}
		return beginSeparator + " ; " + endSeparator;

	}
}
