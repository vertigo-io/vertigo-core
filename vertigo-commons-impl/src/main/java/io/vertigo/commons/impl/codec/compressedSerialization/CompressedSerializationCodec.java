package io.vertigo.commons.impl.codec.compressedSerialization;

import io.vertigo.commons.codec.Codec;
import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;

/**
 * Implémentation standard ThreadSafe gérant les mécanismes permettant de 
 * sérialiser de façon compressée un objet en format binaire (byte[]).
 * 
 * @author pchretien
 * @version $Id: CompressedSerializationCodec.java,v 1.5 2013/11/15 15:27:29 pchretien Exp $
 */
public final class CompressedSerializationCodec implements Codec<Serializable, byte[]> {
	private final Codec<Serializable, byte[]> serializationCodec;
	private final Codec<byte[], byte[]> compressionCodec;

	/**
	 * Constructeur.
	 * @param serializationCodec Codec
	 * @param compressionCodec Codec
	 */
	public CompressedSerializationCodec(final Codec<Serializable, byte[]> serializationCodec, final Codec<byte[], byte[]> compressionCodec) {
		Assertion.checkNotNull(serializationCodec);
		Assertion.checkNotNull(compressionCodec);
		//--------------------------------------------------------------------
		this.serializationCodec = serializationCodec;
		this.compressionCodec = compressionCodec;
	}

	/** {@inheritDoc} */
	public byte[] encode(final Serializable data) {
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		return compressionCodec.encode(serializationCodec.encode(data));

	}

	/** {@inheritDoc} */
	public Serializable decode(final byte[] data) {
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		return serializationCodec.decode(compressionCodec.decode(data));
	}
}
