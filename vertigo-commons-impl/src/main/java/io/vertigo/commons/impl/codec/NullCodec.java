package io.vertigo.commons.impl.codec;

import io.vertigo.commons.codec.Codec;
import io.vertigo.kernel.lang.Assertion;

/**
 *
 * @author pchretien
 */
final class NullCodec<S, T> implements Codec<S, T> {
	private final Codec<S, T> delegateCodec;

	NullCodec(final Codec<S, T> delegateCodec) {
		Assertion.checkNotNull(delegateCodec);
		//-----------------------------------------------------------------
		this.delegateCodec = delegateCodec;
	}

	/** {@inheritDoc} */
	public S decode(final T encoded) {
		if (encoded == null) {
			return null;
		}
		return delegateCodec.decode(encoded);
	}

	/** {@inheritDoc} */
	public T encode(final S toEncode) {
		if (toEncode == null) {
			return null;
		}
		return delegateCodec.encode(toEncode);
	}
}
