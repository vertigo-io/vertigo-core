package io.vertigo.commons.codec;

/**
 * Utilitaire permettant de passer d'un format à un autre format.
 * - format source > encodage > format cible
 * - format cible > décodage > format source.
 * 
 * Les codecs sont nécessairements bijectifs à contrario des encoders.
 * 
 * @author  pchretien
 * @version $Id: Codec.java,v 1.4 2013/11/15 15:31:39 pchretien Exp $
 * @param <S> Type Source à encoder
 * @param <T> Type cible, résultat de l'encodage
 */
public interface Codec<S, T> extends Encoder<S, T> {
	/**
	 * Décodage.
	 * @param encoded Chaîne encodée
	 * @return Objet décodé
	 */
	S decode(T encoded);
}
