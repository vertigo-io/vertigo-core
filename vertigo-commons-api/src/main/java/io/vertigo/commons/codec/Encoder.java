package io.vertigo.commons.codec;

/**
 * Utilitaire permettant de passer d'un format à un autre format.
 * - format source > encodage > format cible
 * 
 * La plupart des codecs sont bijectifs (encodage/ décodage).
 * Certains codecs peuvent ne pas implémenter la fonction de décodage.
 * C'est notamment le cas des calculs d'empreinte. 
 * 
 * @author  pchretien
 * @param <S> Type Source à encoder
 * @param <T> Type cible, résultat de l'encodage
 */
public interface Encoder<S, T> {
	/**
	 * Encodage.
	 * @param toEncode Object à encoder
	 * @return Chaîne codée
	 */
	T encode(S toEncode);
}
