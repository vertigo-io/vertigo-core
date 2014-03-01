package io.vertigo.commons.codec.crypto;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.kernel.exception.VRuntimeException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test des codesc de cryptographie.
 * @author pchretien
 * $Id: AbstractCryptoCodecTest.java,v 1.6 2013/11/15 15:50:56 pchretien Exp $
 */
public abstract class AbstractCryptoCodecTest extends AbstractCodecTest<byte[], byte[]> {

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
		for (int i = 0; i < 30000; i++) {
			Assert.assertNotNull(codec.encode(TEXT.getBytes()));
		}
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testDecode() throws Exception {
		final byte[] encryptedValue = codec.encode(TEXT.getBytes());
		for (int i = 0; i < 30000; i++) {
			Assert.assertEquals(TEXT, new String(codec.decode(encryptedValue)));
		}
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = VRuntimeException.class)
	public void testFailDecode() throws Exception {
		// object ne correspondant pas à une classe;
		final byte[] s = "qdfsdf".getBytes();
		codec.decode(s);
	}
}
