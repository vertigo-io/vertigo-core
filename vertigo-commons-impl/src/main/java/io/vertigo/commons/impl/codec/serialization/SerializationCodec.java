package io.vertigo.commons.impl.codec.serialization;

import io.vertigo.commons.codec.Codec;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Impl�mentation standard threadSafe des m�canismes permettant de s�rialiser/ d�s�rialiser un objet.
 *
 * @author  mcrouzet, pchretien
 * @version $Id: SerializationCodec.java,v 1.6 2013/11/15 15:27:29 pchretien Exp $
 */
public final class SerializationCodec implements Codec<Serializable, byte[]> {

	/** {@inheritDoc} */
	public byte[] encode(final Serializable object) {
		Assertion.checkNotNull(object);
		//---------------------------------------------------------------------
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			try {
				oos.writeObject(object);
				oos.flush();
			} finally {
				oos.close();
			}
			return baos.toByteArray();
		} catch (final IOException e) {
			throw new VRuntimeException("Serialisation : erreur d'ecriture du flux pour {0}", e, object.getClass().getName());
		}
	}

	/** {@inheritDoc} */
	public Serializable decode(final byte[] serializedObject) {
		Assertion.checkNotNull(serializedObject);
		//---------------------------------------------------------------------
		try {
			final InputStream bais = new ByteArrayInputStream(serializedObject);
			final ObjectInputStream ois = new ObjectInputStream(bais);
			try {
				return (Serializable) ois.readObject();
			} finally {
				ois.close();
			}
		} catch (final IOException e) {
			throw new VRuntimeException("Deserialisation : erreur de lecture du flux", e);
		} catch (final ClassNotFoundException e) {
			throw new VRuntimeException("Deserialisation", e);
		}
	}

}
