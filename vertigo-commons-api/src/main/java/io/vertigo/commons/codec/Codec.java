package io.vertigo.commons.codec;

/**
 * Utilitaire permettant de passer d'un format � un autre format.
 * - format source > encodage > format cible
 * - format cible > d�codage > format source.
 * 
 * Les codecs sont n�cessairements bijectifs � contrario des encoders.
 * 
 * @author  pchretien
 * @version $Id: Codec.java,v 1.4 2013/11/15 15:31:39 pchretien Exp $
 * @param <S> Type Source � encoder
 * @param <T> Type cible, r�sultat de l'encodage
 */
public interface Codec<S, T> extends Encoder<S, T> {
	/**
	 * D�codage.
	 * @param encoded Cha�ne encod�e
	 * @return Objet d�cod�
	 */
	S decode(T encoded);
}
