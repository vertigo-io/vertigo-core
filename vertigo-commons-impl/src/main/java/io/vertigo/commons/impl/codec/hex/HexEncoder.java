package io.vertigo.commons.impl.codec.hex;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.kernel.lang.Assertion;

/**
 * Impl�mentation threadSafe des m�canismes standards d'encodage/d�codage.
 * Hex transforme en chaine de caract�res un tableau d'octets
 * 
 * @author  pchretirn
 * @version $Id: HexEncoder.java,v 1.5 2013/11/15 15:27:29 pchretien Exp $
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
