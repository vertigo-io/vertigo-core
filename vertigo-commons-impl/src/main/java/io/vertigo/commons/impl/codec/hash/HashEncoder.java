package io.vertigo.commons.impl.codec.hash;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Impl�mentation des hachages.
 * L'encodage n'autorise pas les donn�es null.
 * La fonction de d�codage n'existe pas.
 * 
 * @author  pchretien
 * @version $Id: HashEncoder.java,v 1.6 2013/11/15 15:27:29 pchretien Exp $
 */
public final class HashEncoder implements Encoder<byte[], byte[]> {
	/**
	 * M�thode de hashage autoris�es.
	 *
	 */
	public enum Hash {
		/** 
		 * MD5.
		 */
		MD5("MD5"),
		/**
		 * SHA1.
		 */
		SHA1("SHA-1"),
		/**
		 * SHA256.
		 */
		SHA256("SHA-256");
		//--------------------------
		private final String algoName;

		private Hash(final String algoName) {
			this.algoName = algoName;
		}

		/**
		 * @return Nom de l'algorithme. 
		 */
		String getAlgoName() {
			return algoName;
		}
	}

	private final Hash hash;

	/**
	 * Constructeur.
	 * @param hash m�thode de hashage
	 */
	public HashEncoder(final Hash hash) {
		Assertion.checkNotNull(hash);
		//-------------------------------------------------------------------------------
		this.hash = hash;
	}

	/** {@inheritDoc} */
	public byte[] encode(final byte[] data) {
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(hash.getAlgoName());
		} catch (final NoSuchAlgorithmException e) {
			throw new VRuntimeException(hash.getAlgoName(), e);
		}
		digest.update(data);
		return digest.digest();
	}
}
