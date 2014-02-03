package io.vertigo.commons.impl.codec.crypto;

import io.vertigo.commons.codec.Codec;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 * Impl�mentation du cryptage
 * - Triple DES.
 * 
 * @author  alauthier, pchretien
 * @version $Id: CryptoCodec.java,v 1.7 2013/11/15 15:27:29 pchretien Exp $
 */
public final class CryptoCodec implements Codec<byte[], byte[]>, Describable {
	/**
	 * M�thode de hashage autoris�es.
	 *
	 */
	public enum Crypto {
		/**
		 * Triple DES.
		 */
		TripleDES("DESede", 168),
		/**
		 * AES (256bits inacessible sans security policies :http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html)
		 */
		AES("AES", 128);

		//--------------------------
		private final String algoName;
		private final int keySize;

		private Crypto(final String algoName, final int keySize) {
			this.algoName = algoName;
			this.keySize = keySize;
		}

		/**
		 * @return Algorithme � utiliser pour crypter.
		 * Doit �tre coh�rent avec la taille de cl�.
		 */
		String getAlgoName() {
			return algoName;
		}

		/**
		 * @return 	Taille de la cl� de crypto.
		 */
		int getKeySize() {
			return keySize;
		}
	}

	private final Crypto crypto;
	private final Key key;

	/**
	 * Constructeur.
	 * @param crypto Algorithme et Taille de cl� � utiliser pour crypter le contexte.
	 */
	public CryptoCodec(final Crypto crypto) {
		Assertion.checkNotNull(crypto);
		//------------------------------------------------------------------------
		this.crypto = crypto;
		key = createKey();
	}

	private Key createKey() {
		try {
			final KeyGenerator keyGenerator = KeyGenerator.getInstance(crypto.getAlgoName());
			keyGenerator.init(crypto.getKeySize());
			return keyGenerator.generateKey();
		} catch (final java.security.NoSuchAlgorithmException e) {
			throw new VRuntimeException("Crypto", e);
		}
	}

	/**
	 * Encryptage de donn�es.
	 * @param data Donn�es � encrypter
	 * @return Donn�es encrypt�es
	 */
	public byte[] encode(final byte[] data) {
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		try {
			final Cipher cipher = Cipher.getInstance(crypto.getAlgoName());
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (final Exception e) {
			throw new VRuntimeException("Crypto", e);
		}
	}

	/**
	 * D�cryptage de donn�es.
	 * @param data Donn�es � d�crypter
	 * @return Donn�es d�crypt�es
	 */
	public byte[] decode(final byte[] data) {
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		try {
			final Cipher cipher = Cipher.getInstance(crypto.getAlgoName());
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (final Exception e) {
			throw new VRuntimeException("Crypto", e);
		}
	}

	/** {@inheritDoc} */
	public List<ComponentInfo> getInfos() {
		final List<ComponentInfo> componentInfos = new ArrayList<>();
		//---
		componentInfos.add(new ComponentInfo("crypto.algo", crypto.getAlgoName()));
		componentInfos.add(new ComponentInfo("crypto.keySize", crypto.getKeySize()));
		//---
		return componentInfos;
	}
}
