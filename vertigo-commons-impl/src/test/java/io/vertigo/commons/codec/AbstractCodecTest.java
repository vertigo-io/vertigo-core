package io.vertigo.commons.codec;

import io.vertigo.commons.codec.Codec;


/**
 * @author dchallas
 * @param <S> Type Source à encoder
 * @param <T> Type cible, résultat de l'encodage
 */
public abstract class AbstractCodecTest<S, T> extends AbstractEncoderTest<Codec<S, T>, S, T> {
	/**
	 * test l'encodage et de décodage de chaines non null.
	 * @throws Exception si problème
	 */
	public abstract void testDecode() throws Exception;

	/**
	 * test le décodage de chaines non encodées avec l'encodeur.
	 * @throws Exception si problème
	 */
	public abstract void testFailDecode() throws Exception;
}
