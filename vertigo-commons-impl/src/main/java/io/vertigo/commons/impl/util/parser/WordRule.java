package io.vertigo.commons.impl.util.parser;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

/**
 * R�gle a �tat permettant de r�cup�rer un mot. 
 * En pr�cisant :
 * - Soit les caract�res accept�s, 
 * - Soit les caract�res rejet�s.
 * 
 * @author pchretien
 * @version $Id: WordRule.java,v 1.8 2013/10/23 11:32:43 pchretien Exp $
 */
public final class WordRule implements Rule<String> {
	/** Mode de selection des charact�res. */
	public enum Mode {
		/** N'accepte que les caract�res pass�s en param�tre. */
		ACCEPT,
		/** Accepte tout sauf les caract�res pass�s en param�tre. */
		REJECT,
		/** Accepte tout sauf les caract�res pass�s en param�tre. 
		 * Avec la possibilit� d'echaper un caract�re avec le \ */
		REJECT_ESCAPABLE
	}

	private static final char escapeChar = '\\';
	private final String acceptedCharacters;
	private final String rejectedCharacters;
	private final String readableCheckedChar;
	private final boolean emptyAccepted;
	private final Mode mode;

	/**
	 * Constructeur.
	 * @param emptyAccepted Si les mots vides sont accept�s
	 * @param checkedChars Liste des caract�res v�rifi�s
	 * @param mode Indique le comportement du parseur : si les caract�res v�rifi�s sont les seuls accept�s, sinon les seuls rejet�s, et si l'echappement est autoris�
	 */
	public WordRule(final boolean emptyAccepted, final String checkedChars, final Mode mode) {
		this(emptyAccepted, checkedChars, mode, "[" + encode(checkedChars) + "]");
	}

	/**
	 * Constructeur.
	 * @param emptyAccepted Si les mots vides sont accept�s
	 * @param checkedChars Liste des caract�res v�rifi�s
	 * @param mode Indique le comportement du parseur : si les caract�res v�rifi�s sont les seuls accept�s, sinon les seuls rejet�s, et si l'echappement est autoris�
	 * @param readableCheckedChar Expression lisible des caract�res v�rifi�s
	 */
	public WordRule(final boolean emptyAccepted, final String checkedChars, final Mode mode, final String readableCheckedChar) {
		super();
		Assertion.checkNotNull(mode);
		Assertion.checkNotNull(checkedChars);
		Assertion.checkArgNotEmpty(readableCheckedChar);
		//---------------------------------------------------------------------
		this.emptyAccepted = emptyAccepted;
		this.mode = mode;
		if (mode == Mode.ACCEPT) {
			acceptedCharacters = checkedChars;
			rejectedCharacters = "";
		} else {
			acceptedCharacters = "";
			rejectedCharacters = checkedChars;
		}
		this.readableCheckedChar = readableCheckedChar;
	}

	/** {@inheritDoc} */
	public String getExpression() {
		final StringBuilder expression = new StringBuilder();
		if (!acceptedCharacters.isEmpty()) {
			expression.append(readableCheckedChar);
		} else if (!rejectedCharacters.isEmpty()) {
			if (mode == Mode.REJECT_ESCAPABLE) {
				expression.append("(!");
				expression.append(readableCheckedChar);
				expression.append("|\\.)");
			} else {
				expression.append("!");
				expression.append(readableCheckedChar);
			}
		} else if (mode == Mode.REJECT || mode == Mode.REJECT_ESCAPABLE) {//tout
			expression.append(".");
		} else {
			throw new IllegalArgumentException("case not implemented");
		}
		expression.append(emptyAccepted ? "*" : "+");
		return expression.toString();
	}

	/** {@inheritDoc} */
	public Parser<String> createParser() {
		return new Parser<String>() {
			private String word;

			/**
			 * @return Mot trouv� par la r�gle
			 */
			public String get() {
				return word;
			}

			/** {@inheritDoc} */
			public int parse(final String text, final int start) throws NotFoundException {
				int index = start;
				// On v�rifie que le caract�re est contenu dans les caract�res accept�s.
				// On v�rifie que le caract�re n'est pas contenu dans les caract�res rejet�s.
				while (index < text.length() //
						&& (mode != Mode.ACCEPT || acceptedCharacters.indexOf(text.charAt(index)) >= 0) //
						&& (mode == Mode.REJECT_ESCAPABLE && index > 0 && text.charAt(index - 1) == escapeChar || rejectedCharacters.indexOf(text.charAt(index)) < 0)) {
					index++;
				}
				if (!emptyAccepted && index == start) {
					throw new NotFoundException(text, start, null, "Mot respectant {0} attendu", getExpression());
				}
				word = text.substring(start, index);
				if (mode == Mode.REJECT_ESCAPABLE) {
					word = word.replaceAll("\\\\(.)", "$1");
				}
				return index;
			}

		};
	}

	private static String encode(final String chaine) {
		final StringBuilder result = new StringBuilder(chaine);
		StringUtil.replace(result, "\r", "\\r");
		StringUtil.replace(result, "\n", "\\n");
		StringUtil.replace(result, "\t", "\\t");
		StringUtil.replace(result, "[", "\\[");
		StringUtil.replace(result, "]", "\\]");
		StringUtil.replace(result, "''", "\"");
		return result.toString();
	}
}
