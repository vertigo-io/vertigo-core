package io.vertigo.commons.impl.codec.html;

import io.vertigo.commons.codec.Codec;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Encodeur/d�codeur.
 *  - Encode des char en String,
 *  - D�code les String en char,
 *  - Il existe un unique caract�re d'�chappement de type char,
 *  - Les caract�res A-Z a-z 0-9 ne sont jamais encod�s.
 *
 * Dans le cas du HTML le caract�re d'�chappement est &
 *
 * @author  pchretien, npiedeloup
 * @version $Id: AbstractCodec.java,v 1.7 2013/11/15 15:27:29 pchretien Exp $
 */
public abstract class AbstractCodec implements Codec<String, String> {
	private final char startEscape;
	private final char endEscape;
	private final char[] toReplace;

	/**
	 * Liste  des chaines remplacantes tri�e dans le m�me ordre que to_replace .
	 * <br>Cette Liste est remplie dans les fils par la m�thode getCharacters
	 */
	private final String[] replaceBy;
	private final Map<String, Character> replaceByMap;
	private int replaceByMaxSize;

	/**
	 * Constructeur.
	 * G�n�ration des tables de remplacement.
	 * Contr�le des chaines de remplacement
	 * Elles doivent commencer par LE caract�re d'�chappement et finir par le caract�re de terminaison.
	 * @param startEscape Caract�re d'�chappement
	 * @param endEscape Caract�re de terminaison
	 */
	protected AbstractCodec(final char startEscape, final char endEscape) {
		this.startEscape = startEscape;
		this.endEscape = endEscape;
		// tri de la table des caracteres HTML
		final String[] characters = getCharacters();
		Arrays.sort(characters);
		toReplace = new char[characters.length];
		replaceBy = new String[characters.length];
		replaceByMap = new HashMap<>(characters.length);
		for (int i = 0; i < characters.length; i++) {
			// premier caractere dans la table des carat�res a remplacer
			toReplace[i] = characters[i].charAt(0);
			// � partir du troisi�me caractere :
			// dans la table des codes remplacants
			replaceBy[i] = characters[i].substring(2);

			//Pour g�rer le cas des encodages non bijectifs comme l'apostrophe word et windows
			//on donne priorit� � la premiere d�finition pour le d�codage.
			if (!replaceByMap.containsKey(replaceBy[i])) {
				replaceByMap.put(replaceBy[i], toReplace[i]);
				//Assertion.isNull(old, "Double insertion pour {0}", replaceBy[i]);
				Assertion.checkArgument(replaceBy[i].charAt(0) == startEscape, "Les caract�res encod�s ({1}) doivent commencer par le caract�re {0}", startEscape, replaceBy[i]);
				Assertion.checkArgument(replaceBy[i].charAt(replaceBy[i].length() - 1) == endEscape, "Les caract�res encod�s ({1}) doivent terminer par le caract�re {0}", endEscape, replaceBy[i]);
				if (replaceBy[i].length() > replaceByMaxSize) {
					replaceByMaxSize = replaceBy[i].length();
				}
			}
		}
	}

	/**
	 * Ajoute une chaine a un StringBuilder apr�s l'avoir encod�e.
	 * Plus la chaine � encoder est longue,plus les gains de perfs sont sensibles.
	 * @param sb StringBuilder � appender.
	 * @param stringToEncode Chaine � encoder et � ajouter � <CODE>sb</CODE>
	 */
	private void encodeString(final StringBuilder sb, final String stringToEncode) {
		final int len = stringToEncode.length();
		sb.ensureCapacity(sb.length() + len + len / 4);
		//R�serve de la place dans le StringBuilder.
		//L'encodage allonge les donn�es on r�server 25% en plus.

		int index;
		char c;
		//On prend les char les uns apr�s les autres et on les traite en les encodant.
		for (int i = 0; i < len; i++) {
			c = stringToEncode.charAt(i);
			// Optimisation qui repr�sente 90% des cas...
			if (StringUtil.isSimpleLetterOrDigit(c) || !shouldEncode(c, i, stringToEncode)) {
				//Les caract�res A-Z a-z 0-9 ne sont jamais encod�s
				sb.append(c);
			} else {
				// Recherche dans le tableau des caract�res si il existe une chaine d'encodage.
				index = Arrays.binarySearch(toReplace, c);
				if (index >= 0) {
					// si trouve on trouve une chaine d'encodage alors on l'ajoute.
					sb.append(replaceBy[index]);
				} else {
					// sinon on ajoute le caract�re sans le modifier.
					sb.append(c);
				}
			}
		}
	}

	/**
	 * Permet d'optimiser en d�terminant si le caract�re doit-�tre encod�
	 * @param c Caract�re � encoder
	 * @param index Index dans la chaine � encoder
	 * @param stringToEncode Chaine � encoder
	 * @return Si ce charact�re doit �tre encod�
	 */
	protected abstract boolean shouldEncode(final char c, final int index, final String stringToEncode);

	private void decodeString(final StringBuilder sb, final String s) {
		final int len = s.length();
		//R�serve de la place dans le StringBuilder.
		//Le d�codage compresse les donn�es on se contente donc de r�server
		//autant de place  que la longueur de la chaine � d�coder.
		sb.ensureCapacity(sb.length() + len);

		Character decodedChar;
		for (int i = 0; i < len; i++) {
			final char c = s.charAt(i);
			// petite optimisation (qui represente 90% des cas...
			if (c != startEscape) {
				sb.append(c);
			} else {
				//On a trouv� le caract�re d'�chappement
				//On cherche le caract�re de fin d'�chappement
				int j = 0;
				do {
					j++;
					//Tant que l'on n'a pas rouv� le caract�re de fin
					//ou d�pass� le nombre de caract�res max d'encodage ou que l'on est arriv� � la fin.
				} while (j < replaceByMaxSize && i + j < len && s.charAt(i + j) != endEscape);
				//s'il on ne trouve pas la fin on passe, sinon on remplace
				if (j < replaceByMaxSize && i + j < len) {
					//La chaine encod�e se trouve entre l'indice i et l'indice i+j
					//On l'extrait et on cherche le caract�re qu'elle repr�sente.
					final String stringToDecode = s.substring(i, i + j + 1);
					decodedChar = replaceByMap.get(stringToDecode);
					if (decodedChar != null) {
						sb.append(decodedChar);//on a trouv� un caract�re
					} else if (!shouldEncode(c, 0, stringToDecode)) {
						sb.append(stringToDecode);//si le caract�re ne devait pas �tre encod�, on n'essai pas de le d�coder.
					} else {
						throw new IllegalArgumentException("d�codage non trouv� pour " + stringToDecode);
					}
					i += j;
				} else {
					sb.append(s.substring(i, i + j));
					i += j - 1;
				}
			}
		}
	}

	/**
	 * Effectue l'encodage. 
	 * @param s Chaine � encoder
	 * @return Chaine encod�e
	 */
	protected final String doEncode(final String s) {
		Assertion.checkNotNull(s);
		//---------------------------------------------------------------------
		if (s.length() == 0) { // perf
			return "";
		}
		final StringBuilder sb = new StringBuilder(s.length() + s.length() / 4);
		encodeString(sb, s);
		return sb.toString();

	}

	/**
	 * Effectue le d�codage. 
	 * @param s Chaine � d�coder
	 * @return Chaine d�cod�e
	 */
	protected final String doDecode(final String s) {
		Assertion.checkNotNull(s);
		//---------------------------------------------------------------------
		if (s.length() == 0) { // perf
			return "";
		}
		final StringBuilder sb = new StringBuilder(s.length() + s.length() / 4);
		decodeString(sb, s);
		return sb.toString();
	}

	/**
	 * Permet de remplacer des caract�res par des mots.
	 * @return Tableau des �l�ments � remplacer
	 */
	protected abstract String[] getCharacters();
}
