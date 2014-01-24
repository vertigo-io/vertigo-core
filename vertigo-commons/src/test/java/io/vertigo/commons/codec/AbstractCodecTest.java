package io.vertigo.commons.codec;

import io.vertigo.commons.codec.Codec;


/**
 * @author dchallas
 * @param <S> Type Source � encoder
 * @param <T> Type cible, r�sultat de l'encodage
 */
public abstract class AbstractCodecTest<S, T> extends AbstractEncoderTest<Codec<S, T>, S, T> {
	/**
	 * test l'encodage et de d�codage de chaines non null.
	 * @throws Exception si probl�me
	 */
	public abstract void testDecode() throws Exception;

	/**
	 * test le d�codage de chaines non encod�es avec l'encodeur.
	 * @throws Exception si probl�me
	 */
	public abstract void testFailDecode() throws Exception;
}
