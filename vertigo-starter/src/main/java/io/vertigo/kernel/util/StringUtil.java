/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.kernel.util;

import io.vertigo.kernel.lang.Assertion;

import java.text.MessageFormat;


/**
 * Classe utilitaire proposant des m�thodes de manipulation sur les String.
 *
 * @author  pchretien
 */
public final class StringUtil {
	/**
	 * Constructeur priv� pour classe utilitaire
	 */
	private StringUtil() {
		//RAS
	}

	/**
	 * Impl�mentation du test de la chaine vide.
	 * ie null ou blank (espace, \t \n \r \p ...)
	 * @param strValue String
	 * @return Si la chaine apr�s trim est null ou vide
	 */
	public static boolean isEmpty(final String strValue) {
		if (strValue == null) {
			return true;
		}
		//On prefere cette implementation qui ne cr�e pas de nouvelle chaine (contrairement au trim())
		for (int i = 0; i < strValue.length(); i++) {
			if (strValue.charAt(i) > ' ') {
				return false;
			}
		}
		return true;
	}

	/**
	 * On normalise les id.
	 * @param strValue String non null
	 * @return id normalis�
	 */
	public static String normalize(final String strValue) {
		Assertion.checkNotNull(strValue);
		//---------------------------------------------------------------------
		return first2LowerCase(strValue);
	}

	/**
	 * On abaisse la premiere lettre.
	 * @param strValue String non null
	 * @return Chaine avec la premiere lettre en minuscule
	 */
	public static String first2LowerCase(final String strValue) {
		Assertion.checkNotNull(strValue);
		//---------------------------------------------------------------------
		if (strValue.isEmpty()) {
			return strValue;
		}

		final char firstChar = strValue.charAt(0);
		if (Character.isUpperCase(firstChar)) { //la m�thode est appell� souvant et la concat�nation de chaine est lourde : on test avant de faire l'op�ration
			return Character.toLowerCase(firstChar) + strValue.substring(1);
		}
		return strValue;
	}

	/**
	 * Capitalisation de la premi�re lettre.
	 *
	 * @param strValue String non null
	 * @return Chaine avec la premiere lettre en majuscule
	 */
	public static String first2UpperCase(final String strValue) {
		Assertion.checkNotNull(strValue);
		//---------------------------------------------------------------------
		if (strValue.isEmpty()) {
			return strValue;
		}

		final char firstChar = strValue.charAt(0);
		if (Character.isLowerCase(firstChar)) { //la m�thode est appell� souvant et la concat�nation de chaine est lourde : on test avant de faire l'op�ration
			return Character.toUpperCase(firstChar) + strValue.substring(1);
		}
		return strValue;
	}

	/**
	 * XXX_YYY_ZZZ -> XxxYyyZzz ou xxxYyyZzz.
	 * @param str la chaine de carat�res sur laquelle s'appliquent les transformation
	 * @param first2UpperCase d�finit si la premi�re lettre est en majuscules
	 * @return Renvoie une chaine de carat�re correspondant � str en minuscule et sans underscores,
	 * � l'exception des premi�res lettres apr�s les underscores dans str
	 */
	public static String constToCamelCase(final String str, final boolean first2UpperCase) {
		Assertion.checkNotNull(str);
		Assertion.checkArgument(str.length() > 0, "Chaine � modifier invalide (ne doit pas �tre vide)");
		Assertion.checkArgument(str.indexOf("__") == -1, "Chaine � modifier invalide : {0} (__ interdit)", str);
		// ----------------------------------------------------------------------
		final StringBuilder result = new StringBuilder();
		boolean upper = first2UpperCase;
		Boolean digit = null;
		final int length = str.length();
		char c;
		for (int i = 0; i < length; i++) {
			c = str.charAt(i);
			if (c == '_') {
				if (digit != null && digit.booleanValue() && Character.isDigit(str.charAt(i + 1))) {
					result.append('_');
				}
				digit = null;
				upper = true;
			} else {
				if (digit != null) {
					Assertion.checkArgument(digit.equals(Character.isDigit(c)), "Chaine � modifier invalide : {0} (lettres et chiffres doivent toujours �tre s�par�s par _)", str);
				}
				digit = Character.isDigit(c);

				if (upper) {
					result.append(Character.toUpperCase(c));
					upper = false;
				} else {
					result.append(Character.toLowerCase(c));
				}
			}
		}
		return result.toString();
	}

	/**
	 * Les chiffres sont assimil�s � des lettres en majuscules
	 * XxxYyyZzz ou xxxYyyZzz -> XXX_YYY_ZZZ
	 * XxxYZzz ou xxxYZzz -> XXX_Y_ZZZ
	 * Xxx123 -->XXX_123
	 * XxxYzw123 --> (interdit)
	 * Xxx123Y --> XXX_123_Y.
	 * Xxx123y --> XXX_123Y.
	 * @param str la chaine de carat�res sur laquelle s'appliquent les transformation
	 * @return Passage en constante d'une cha�ne de caract�res (Fonction inverse de caseTransform)
	 */
	public static String camelToConstCase(final String str) {
		Assertion.checkNotNull(str);
		Assertion.checkArgument(str.length() > 0, "Chaine � modifier invalide");
		// ----------------------------------------------------------------------
		final StringBuilder result = new StringBuilder();
		final int length = str.length();
		char c;
		boolean upperCase = true;
		boolean isDigit = false;
		for (int i = 0; i < length; i++) {
			c = str.charAt(i);
			if (Character.isDigit(c) || c == '_') {
				if (!isDigit && !upperCase) {
					isDigit = true;
					if (!upperCase) {
						result.append('_');
					}
				}
			} else if (Character.isUpperCase(c)) {
				if (!upperCase || (upperCase && i==1)) {
					//upperCase = true;
					result.append('_');
				}
			} else {
				upperCase = false;
				isDigit = false;
			}
			result.append(Character.toUpperCase(c));
		}
		return result.toString();
	}

	/**
	 * Teste si un caract�re est une simple lettre (minuscule ou majuscule, sans accent)
	 * ou un chiffre.
	 * @param c caract�re
	 * @return boolean
	 */
	public static boolean isSimpleLetterOrDigit(final char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}

	/**
	 * Remplacement au sein d'une chaine d'un motif par un autre.
	 * Le remplacement avance, il n'est pas r�cursif !!.
	 * Attention : pour des char le String.replace(char old, char new) est plus performant.
	 * 
	 * @param str String
	 * @param oldStr Chaine � remplacer
	 * @param newStr Chaine de remplacement
	 * @return Chaine remplac�e
	 */
	public static String replace(final String str, final String oldStr, final String newStr) {
		Assertion.checkNotNull(str);
		//------------------------------------------------------------------------
		final StringBuilder result = new StringBuilder(str);
		replace(result, oldStr, newStr);
		return result.toString();
	}

	/**
	 * Remplacement au sein d'une chaine d'un motif par un autre.
	 * Le remplacement avance, il n'est pas r�cursif !!.
	 * Le StringBuilder est modifi� !! c'est pourquoi il n'y a pas de return.
	 * @param str StringBuilder
	 * @param oldStr Chaine � remplacer
	 * @param newStr Chaine de remplacement
	 */
	public static void replace(final StringBuilder str, final String oldStr, final String newStr) {
		Assertion.checkNotNull(str);
		Assertion.checkNotNull(oldStr);
		Assertion.checkArgument(oldStr.length() > 0, "La chaine a remplacer ne doit pas �tre vide");
		Assertion.checkNotNull(newStr);
		//------------------------------------------------------------------------
		int index = str.indexOf(oldStr);
		if (index == -1) {
			return;
		}

		final int oldStrLength = oldStr.length();
		final int newStrLength = newStr.length();
		StringBuilder result = str;
		do {
			result = result.replace(index, index + oldStrLength, newStr);
			index = str.indexOf(oldStr, index + newStrLength);
		} while (index != -1);
	}

	/**
	 * Fusionne une chaine compatible avec les param�tres.
	 * Les caract�res { }  sont interdits ou doivent �tre echapp�s avec \\.
	 * @param msg Chaine au format MessageFormat
	 * @param params Param�tres du message
	 * @return Chaine fusionn�e
	 */
	public static String format(final String msg, final Object... params) {
		Assertion.checkNotNull(msg);
		//------------------------------------------------------------------------
		if (params == null || params.length == 0) {
			return msg;
		}
		//Gestion des doubles quotes 
		//On simple quotes les doubles quotes d�j� pos�es.
		//Puis on double toutes les simples quotes ainsi il ne reste plus de simple quote non doubl�e.
		final StringBuilder newMsg = new StringBuilder(msg);
		replace(newMsg, "''", "'");
		replace(newMsg, "'", "''");
		replace(newMsg, "\\{", "'{'");
		replace(newMsg, "\\}", "'}'");
		return MessageFormat.format(newMsg.toString(), params);
	}
}
