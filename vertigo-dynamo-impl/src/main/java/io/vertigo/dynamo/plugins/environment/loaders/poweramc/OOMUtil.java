package io.vertigo.dynamo.plugins.environment.loaders.poweramc;

import io.vertigo.kernel.lang.Assertion;

/**
 * Utilitaire permettant de créer un nom de méthode Java à partir d'un texte libre.
 *
 * @author pchretien
 * @version $Id: OOMUtil.java,v 1.4 2013/10/22 10:44:56 pchretien Exp $
 */
final class OOMUtil {
	private OOMUtil() {
		//Rien 
	}

	/**
	 * Conversion français vers Java avec remplacement d'accents : Xxx éèà zzz -> XxxEeaZzz.
	 * @param str la chaine de caratères sur laquelle s'appliquent les transformation
	 * @return Renvoie le résultat
	 */
	static String french2Java(final String str) {
		Assertion.checkNotNull(str);
		Assertion.checkArgument(str.length() > 0, "La chaine à modifier ne doit pas être vide.");
		// ----------------------------------------------------------------------
		final StringBuilder suffix = new StringBuilder();
		int i = 1;
		char c;
		c = replaceAccent(str.charAt(0));
		suffix.append(Character.toUpperCase(c));

		final int length = str.length();
		while (i < length) {
			c = str.charAt(i);
			//On considère blanc, et ' comme des séparateurs de mots.
			if (c == ' ' || c == '\'') {
				if (i + 1 < length) {
					c = replaceAccent(str.charAt(i + 1));
					if (Character.isLetterOrDigit(c)) {
						suffix.append(Character.toUpperCase(c));
					}
					i += 2;
				} else {
					i++; // évitons boucle infinie
				}
			} else {
				c = replaceAccent(c);
				if (Character.isLetterOrDigit(c)) {
					suffix.append(c);
				}
				i++;
			}
		}
		return suffix.toString();
	}

	/**
	 * Remplacement de caractères accentués par leurs équivalents non accentués
	 * (par ex: accents dans rôles)
	 * @param c caractère accentué à traiter
	 * @return caractère traité (sans accent)
	 */
	private static char replaceAccent(final char c) {
		char result;
		switch (c) {
			case '\u00e0':
			case '\u00e2':
			case '\u00e4':
				result = 'a';
				break;
			case '\u00e7':
				result = 'c';
				break;
			case '\u00e8':
			case '\u00e9':
			case '\u00ea':
			case '\u00eb':
				result = 'e';
				break;
			case '\u00ee':
			case '\u00ef':
				result = 'i';
				break;
			case '\u00f4':
			case '\u00f6':
				result = 'o';
				break;
			case '\u00f9':
			case '\u00fb':
			case '\u00fc':
				result = 'u';
				break;
			default:
				result = c;
				break;
		}

		return result;
	}
}
