package io.vertigo.commons.codec.hash;

import io.vertigo.commons.codec.AbstractEncoderTest;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test l'encodeur de Hash.
 * @author pchretien
 * $Id: HashCodecMD5Test.java,v 1.4 2013/11/15 15:50:56 pchretien Exp $
 */
public final class HashCodecMD5Test extends AbstractEncoderTest<Encoder<byte[], byte[]>, byte[], byte[]> {
	/** {@inheritDoc} */
	@Override
	public Encoder<byte[], byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getMD5Encoder();
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = NullPointerException.class)
	public void testNull() {
		/*
		* Test de cr�ation de l'empreinte MD5.
		* On v�rifie que null ne respecte pas le contrat.
		*/
		codec.encode(null);
	}

	/**
	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		//Caract�res simples sans encodage
		final Encoder<byte[], String> hexEncoder = getCodecManager().getHexEncoder();
		final byte[] encoded = codec.encode(TEXT.getBytes());
		Assert.assertEquals(128 / 8, codec.encode(TEXT.getBytes()).length);
		Assert.assertEquals("8e51e6dffd416849980ec365bee3f8bd", hexEncoder.encode(encoded));
	}
}
