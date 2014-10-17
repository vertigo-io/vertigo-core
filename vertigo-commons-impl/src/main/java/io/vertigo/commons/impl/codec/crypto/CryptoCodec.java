/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.commons.impl.codec.crypto;

import io.vertigo.commons.codec.Codec;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.spaces.component.ComponentInfo;
import io.vertigo.core.spaces.component.Describable;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 * Implémentation du cryptage
 * - Triple DES.
 * 
 * @author  alauthier, pchretien
 */
public final class CryptoCodec implements Codec<byte[], byte[]>, Describable {
	/**
	 * Méthode de hashage autorisées.
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
		 * @return Algorithme à utiliser pour crypter.
		 * Doit être cohérent avec la taille de clé.
		 */
		String getAlgoName() {
			return algoName;
		}

		/**
		 * @return 	Taille de la clé de crypto.
		 */
		int getKeySize() {
			return keySize;
		}
	}

	private final Crypto crypto;
	private final Key key;

	/**
	 * Constructeur.
	 * @param crypto Algorithme et Taille de clé à utiliser pour crypter le contexte.
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
			throw new RuntimeException("Crypto", e);
		}
	}

	/**
	 * Encryptage de données.
	 * @param data Données à encrypter
	 * @return Données encryptées
	 */
	public byte[] encode(final byte[] data) {
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		try {
			final Cipher cipher = Cipher.getInstance(crypto.getAlgoName());
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (final Exception e) {
			throw new RuntimeException("Crypto", e);
		}
	}

	/**
	 * Décryptage de données.
	 * @param data Données à décrypter
	 * @return Données décryptées
	 */
	public byte[] decode(final byte[] data) {
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		try {
			final Cipher cipher = Cipher.getInstance(crypto.getAlgoName());
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (final Exception e) {
			throw new RuntimeException("Crypto", e);
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
