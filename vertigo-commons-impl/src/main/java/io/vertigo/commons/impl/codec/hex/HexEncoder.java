package io.vertigo.commons.impl.codec.hex;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation threadSafe des mécanismes standards d'encodage/décodage.
 * Hex transforme en chaine de caractères un tableau d'octets
 * 
 * @author  pchretirn
 */
public final class HexEncoder implements Encoder<byte[], String> {

	/** {@inheritDoc} */
	public String encode(final byte[] data) {
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		final StringBuilder output = new StringBuilder(data.length * 2);
		for (final byte element : data) {
			output.append(Integer.toHexString(element >> 4 & 0xf)).append(Integer.toHexString(element & 0xf));
		}
		return output.toString();
	}
}
