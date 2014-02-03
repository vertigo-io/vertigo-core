package io.vertigo.commons.codec.crypto;

import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;

/**
 * Test des codesc de cryptographie.
 * @author pchretien
 * $Id: AES128CodecTest.java,v 1.3 2013/11/15 15:50:56 pchretien Exp $
 */
public final class AES128CodecTest extends AbstractCryptoCodecTest {
	/** {@inheritDoc} */
	@Override
	public Codec<byte[], byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getAES128Codec();
	}
}
