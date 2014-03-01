package io.vertigo.commons.codec.compression;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.codec.compression.CompressionCodec;
import io.vertigo.kernel.exception.VRuntimeException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test du codec de compresion.
 * 
 * @author pchretien
 * @version $Id: CompressionCodecTest.java,v 1.6 2013/11/15 15:50:56 pchretien Exp $
 */
public final class CompressionCodecTest extends AbstractCodecTest<byte[], byte[]> {
	/** {@inheritDoc} */
	@Override
	public Codec<byte[], byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getCompressionCodec();
	}

	/**
	 * Test des mécanismes de compression/décompression des valeurs null.
	 ** 
	 */
	@Override
	@Test
	public void testNull() {
		Assert.assertNull(codec.encode(null));
		Assert.assertNull(codec.decode(null));
	}

	/**
	 * Test des mécanismes de compression/décompression.
	 ** 
	 */
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
		Assert.assertEquals(TEXT, new String(codec.decode(encodedValue)));

	}

	@Test
	public void testUncompressedDecode() {
		// object ne correspondant pas à une classe;
		final byte[] s = "qdfsdf".getBytes();
		Assert.assertTrue(s.length < CompressionCodec.MIN_SIZE_FOR_COMPRESSION);
		final byte[] result = codec.decode(s);
		Assert.assertEquals("qdfsdf", new String(result));
	}

	@Test
	public void testNopDecode() {
		// object sans préfixe de compression, est laissé tel quel;
		final byte[] s = "qdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdf".getBytes();
		Assert.assertTrue(s.length > CompressionCodec.MIN_SIZE_FOR_COMPRESSION);
		final byte[] result = codec.decode(s);
		Assert.assertEquals(new String(s), new String(result));
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = VRuntimeException.class)
	public void testFailDecode() throws Exception {
		// object avec prefix ne correspondant pas à une classe;
		final byte[] s = "COMPqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdfqdfsdf".getBytes();
		Assert.assertTrue(s.length > CompressionCodec.MIN_SIZE_FOR_COMPRESSION);
		/* final byte[] result = */

		//Le decodage lance une exception
		codec.decode(s);
		Assert.fail();
	}
}