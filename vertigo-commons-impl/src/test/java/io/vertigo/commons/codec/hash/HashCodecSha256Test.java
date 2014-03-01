package io.vertigo.commons.codec.hash;

import io.vertigo.commons.codec.AbstractEncoderTest;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test l'encodeur de Hash.
 * @author pchretien
 * $Id: HashCodecSha256Test.java,v 1.4 2013/11/15 15:50:56 pchretien Exp $
 */
public final class HashCodecSha256Test extends AbstractEncoderTest<Encoder<byte[], byte[]>, byte[], byte[]> {
	/** {@inheritDoc} */
	@Override
	public Encoder<byte[], byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getSha256Encoder();
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = NullPointerException.class)
	public void testNull() {
		/*
		 * Test de création de l'empreinte SHA 1.
		 * On vérifie que null ne respecte pas le contrat.
		 */
		codec.encode(null);
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		//Caractères simples sans encodage
		//256 bits attendus soit 32 Octets
		Assert.assertEquals(32, codec.encode(TEXT.getBytes()).length);
	}
}