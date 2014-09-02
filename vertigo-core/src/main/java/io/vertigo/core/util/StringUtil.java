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
package io.vertigo.core.util;

import io.vertigo.core.lang.Assertion;

import java.text.MessageFormat;

/**
 * Classe utilitaire proposant des méthodes de manipulation sur les String.
 *
 * @author  pchretien
 */
public final class StringUtil {
	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private StringUtil() {
		//RAS
	}

	/**
	 * Implémentation du test de la chaine vide.
	 * ie null ou blank (espace, \t \n \r \p ...)
	 * @param strValue String
	 * @return Si la chaine aprés trim est null ou vide
	 */
	public static boolean isEmpty(final String strValue) {
		if (strValue == null) {
			return true;
		}
		//On prefere cette implementation qui ne crée pas de nouvelle chaine (contrairement au trim())
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
	 * @return id normalisé
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
		if (Character.isUpperCase(firstChar)) { //la méthode est appellé souvant et la concaténation de chaine est lourde : on test avant de faire l'opération
			return Character.toLowerCase(firstChar) + strValue.substring(1);
		}
		return strValue;
	}

	/**
	 * Capitalisation de la première lettre.
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
		if (Character.isLowerCase(firstChar)) { //la méthode est appellé souvant et la concaténation de chaine est lourde : on test avant de faire l'opération
			return Character.toUpperCase(firstChar) + strValue.substring(1);
		}
		return strValue;
	}

	/**
	 * XXX_YYY_ZZZ -> XxxYyyZzz ou xxxYyyZzz.
	 * @param str la chaine de caratéres sur laquelle s'appliquent les transformation
	 * @param first2UpperCase définit si la première lettre est en majuscules
	 * @return Renvoie une chaine de caratére correspondant à str en minuscule et sans underscores,
	 * à l'exception des premières lettres aprés les underscores dans str
	 */
	public static String constToCamelCase(final String str, final boolean first2UpperCase) {
		Assertion.checkNotNull(str);
		Assertion.checkArgument(str.length() > 0, "Chaine à modifier invalide (ne doit pas être vide)");
		Assertion.checkArgument(str.indexOf("__") == -1, "Chaine à modifier invalide : {0} (__ interdit)", str);
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
					Assertion.checkArgument(digit.equals(Character.isDigit(c)), "Chaine à modifier invalide : {0} (lettres et chiffres doivent toujours être séparés par _)", str);
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
	 * Les chiffres sont assimilés à des lettres en majuscules
	 * XxxYyyZzz ou xxxYyyZzz -> XXX_YYY_ZZZ
	 * XxxYZzz ou xxxYZzz -> XXX_Y_ZZZ
	 * Xxx123 -->XXX_123
	 * XxxYzw123 --> (interdit)
	 * Xxx123Y --> XXX_123_Y.
	 * Xxx123y --> XXX_123Y.
	 * @param str la chaine de caratéres sur laquelle s'appliquent les transformation
	 * @return Passage en constante d'une chaîne de caractères (Fonction inverse de caseTransform)
	 */
	public static String camelToConstCase(final String str) {
		Assertion.checkNotNull(str);
		Assertion.checkArgument(str.length() > 0, "Chaine à modifier invalide");
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
				if (!upperCase || (upperCase && i == 1)) {
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
	 * Teste si un caractère est une simple lettre (minuscule ou majuscule, sans accent)
	 * ou un chiffre.
	 * @param c caractère
	 * @return boolean
	 */
	public static boolean isSimpleLetterOrDigit(final char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}

	/**
	 * Remplacement au sein d'une chaine d'un motif par un autre.
	 * Le remplacement avance, il n'est pas récursif !!.
	 * Attention : pour des char le String.replace(char old, char new) est plus performant.
	 * 
	 * @param str String
	 * @param oldStr Chaine à remplacer
	 * @param newStr Chaine de remplacement
	 * @return Chaine remplacée
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
	 * Le remplacement avance, il n'est pas récursif !!.
	 * Le StringBuilder est modifié !! c'est pourquoi il n'y a pas de return.
	 * @param str StringBuilder
	 * @param oldStr Chaine à remplacer
	 * @param newStr Chaine de remplacement
	 */
	public static void replace(final StringBuilder str, final String oldStr, final String newStr) {
		Assertion.checkNotNull(str);
		Assertion.checkNotNull(oldStr);
		Assertion.checkArgument(oldStr.length() > 0, "La chaine a remplacer ne doit pas être vide");
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
	 * Fusionne une chaine compatible avec les paramètres.
	 * Les caractères { }  sont interdits ou doivent être echappés avec \\.
	 * @param msg Chaine au format MessageFormat
	 * @param params paramètres du message
	 * @return Chaine fusionnée
	 */
	public static String format(final String msg, final Object... params) {
		Assertion.checkNotNull(msg);
		//------------------------------------------------------------------------
		if (params == null || params.length == 0) {
			return msg;
		}
		//Gestion des doubles quotes 
		//On simple quotes les doubles quotes déjà posées.
		//Puis on double toutes les simples quotes ainsi il ne reste plus de simple quote non doublée.
		final StringBuilder newMsg = new StringBuilder(msg);
		replace(newMsg, "''", "'");
		replace(newMsg, "'", "''");
		replace(newMsg, "\\{", "'{'");
		replace(newMsg, "\\}", "'}'");
		return MessageFormat.format(newMsg.toString(), params);
	}
}
