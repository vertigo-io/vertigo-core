package io.vertigo.commons.codec;

/**
 * Utilitaire permettant de passer d'un format � un autre format.
 * - format source > encodage > format cible
 * 
 * La plupart des codecs sont bijectifs (encodage/ d�codage).
 * Certains codecs peuvent ne pas impl�menter la fonction de d�codage.
 * C'est notamment le cas des calculs d'empreinte. 
 * 
 * @author  pchretien
 * @version $Id: Encoder.java,v 1.4 2013/11/15 15:31:39 pchretien Exp $
 * @param <S> Type Source � encoder
 * @param <T> Type cible, r�sultat de l'encodage
 */
public interface Encoder<S, T> {
	/**
	 * Encodage.
	 * @param toEncode Object � encoder
	 * @return Cha�ne cod�e
	 */
	T encode(S toEncode);
}
