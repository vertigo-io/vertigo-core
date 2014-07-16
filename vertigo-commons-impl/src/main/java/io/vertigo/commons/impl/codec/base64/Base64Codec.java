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
package io.vertigo.commons.impl.codec.base64;

import io.vertigo.commons.codec.Codec;
import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation threadSafe des mécanismes standards d'encodage/décodage.
 * Base 64 modifié pour passer dans les urls ('+', '/' remplacer par '_', '-' )
 * Les codes sont gérés par quatre octets. 
 * {voir wikipedia http://en.wikipedia.org/wiki/Base64#Privacy-Enhanced_Mail_.28PEM.29}
 * @author  npiedeloup
 */
public final class Base64Codec implements Codec<byte[], String> {
	private static final char PADDING = '=';
	private static final char[] ENCODE_TABLE = { //
	'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', //
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',//
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '-', };
	private static final int[] DECODE_TABLE;
	static {
		DECODE_TABLE = new int[256];
		for (int i = 0; i < DECODE_TABLE.length; i++) {
			DECODE_TABLE[i] = -1;
		}
		for (int i = 0; i < ENCODE_TABLE.length; i++) {
			DECODE_TABLE[ENCODE_TABLE[i]] = i;
		}
	}

	/** {@inheritDoc} */
	public byte[] decode(final String coded) {
		Assertion.checkNotNull(coded);
		//---------------------------------------------------------------------
		final int length = coded.length();
		if (length == 0) {
			return new byte[0];
		}
		if (length % 4 != 0) {
			throw new RuntimeException("Données transmises malformées");
		}
		for (int i = 0; i < coded.length(); i++) {
			if (coded.charAt(i) != PADDING && DECODE_TABLE[coded.charAt(i)] == -1) {
				throw new RuntimeException("Données transmises malformées");
			}
		}
		// ----
		final int mod; // = coded.charAt(0) - '0';
		if (PADDING == coded.charAt(length - 2)) {
			mod = 2;
		} else if (PADDING == coded.charAt(length - 1)) {
			mod = 1;
		} else {
			mod = 0;
		}
		final int len = length / 4 * 3 - mod;/*(mod == 0 ? 0 : (3 - mod));*/
		final int len1 = len - 1;
		final int len2 = len - 2;
		final byte[] res = new byte[len];
		decodeCharacters(coded, length, len1, len2, res);
		return res;
	}

	private void decodeCharacters(final String coded, final int length, final int len1, final int len2, final byte[] res) {
		final int[] b = new int[4];
		//int[] e = new int[3];
		int pos = 0;

		for (int i = 0; i < length; i += 4) {
			// on part de i=1 puisque le caractère 0 est la longeur modulo 3 (voir encode)
			for (int j = 0; j < 4; j++) {
				b[j] = DECODE_TABLE[coded.charAt(i + j)];
			}
			res[pos] = (byte) ((b[0] << 2 | b[1] >> 4) & 0xFF);
			if (pos < len1) {
				res[pos + 1] = (byte) ((b[1] << 4 | b[2] >> 2) & 0xFF);
				if (pos < len2) {
					res[pos + 2] = (byte) ((b[2] << 6 | b[3]) & 0xFF);
				}
			}
			pos += 3;
		}
	}

	/** {@inheritDoc} */
	public String encode(final byte[] raw) {
		Assertion.checkNotNull(raw);
		//---------------------------------------------------------------------
		/*
		 * Encode une série d'octets en base 64.
		 */
		final int mod = raw.length % 3;
		final int len = raw.length;
		final StringBuilder res = new StringBuilder((len / 3 + mod == 0 ? 0 : 1) * 4);

		final int[] e = new int[3];
		for (int i = 0; i < len; i += 3) {
			e[0] = raw[i] & 0xFF;
			e[1] = (i + 1 < len ? raw[i + 1] : 0) & 0xFF;
			e[2] = (i + 2 < len ? raw[i + 2] : 0) & 0xFF;
			res.append(ENCODE_TABLE[e[0] >> 2]);
			res.append(ENCODE_TABLE[(e[0] << 4 | e[1] >> 4) & 0x3F]);
			res.append(i + 1 < len ? ENCODE_TABLE[(e[1] << 2 | e[2] >> 6) & 0x3F] : PADDING);
			res.append(i + 2 < len ? ENCODE_TABLE[e[2] & 0x3F] : PADDING);
		}
		return res.toString();
	}
}
