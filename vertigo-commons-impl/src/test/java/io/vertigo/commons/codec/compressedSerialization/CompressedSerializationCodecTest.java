package io.vertigo.commons.codec.compressedSerialization;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test du codec de compresion.
 * 
 * @author pchretien
 */
public final class CompressedSerializationCodecTest extends AbstractCodecTest<Serializable, byte[]> {

	/** {@inheritDoc} */
	@Override
	public Codec<Serializable, byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getCompressedSerializationCodec();
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testNull() {
		Assert.assertNull(codec.encode(null));
		Assert.assertNull(codec.decode(null));
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		Assert.assertNotNull(codec.encode(TEXT.getBytes()));

	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testDecode() throws Exception {
		final byte[] encodedValue = codec.encode(TEXT.getBytes());
		Assert.assertEquals(TEXT, new String((byte[]) codec.decode(encodedValue)));

	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testFailDecode() throws Exception {
		//
	}

}
