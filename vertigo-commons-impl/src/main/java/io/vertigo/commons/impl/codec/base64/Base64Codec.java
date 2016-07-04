/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
import io.vertigo.lang.Assertion;

/**
 * Implémentation threadSafe des mécanismes standards d'encodage/décodage.
 * Base 64 modifié pour passer dans les urls ('+', '/' remplacer par '_', '-' )
 * Les codes sont gérés par quatre octets.
 * {voir wikipedia http://en.wikipedia.org/wiki/Base64#Privacy-Enhanced_Mail_.28PEM.29}
 * @author  npiedeloup
 */
public final class Base64Codec implements Codec<byte[], String> {
	private static final int OFFSET_3 = 3;
	private static final int OFFSET_2 = 2;
	private static final int OFFSET_1 = 1;
	private static final int OFFSET_0 = 0;
	private static final int SHIFT_1BIT = 2; //shift octet 1 bit left or right
	private static final int SHIFT_2BIT = 4; //shift octet 2 bits left or right
	private static final int SHIFT_3BIT = 6; //shift octet 3 bits left or right

	private static final int BASE64_ENCODED_BLOCK_LEN = 4; //Length of base64 encoded block
	private static final int BASE64_DECODED_BLOCK_LEN = 3; //Length of base64 decoded block
	private static final int BASE64_NB_CHAR = 256; //Number of supported decoded char (all chars = 1 byte)

	private static final char PADDING = '=';
	private static final char[] ENCODE_TABLE = {
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '-', };
	private static final int[] DECODE_TABLE;
	static {
		DECODE_TABLE = new int[BASE64_NB_CHAR];
		for (int i = 0; i < DECODE_TABLE.length; i++) {
			DECODE_TABLE[i] = -1;
		}
		for (int i = 0; i < ENCODE_TABLE.length; i++) {
			DECODE_TABLE[ENCODE_TABLE[i]] = i;
		}
	}

	/** {@inheritDoc} */
	@Override
	public byte[] decode(final String coded) {
		Assertion.checkNotNull(coded);
		//-----
		final int length = coded.length();
		if (length == 0) {
			return new byte[0];
		}
		if (length % BASE64_ENCODED_BLOCK_LEN != 0) {
			throw new IllegalArgumentException("Données transmises malformées");
		}
		for (int i = 0; i < coded.length(); i++) {
			if (coded.charAt(i) != PADDING && DECODE_TABLE[coded.charAt(i)] == -1) {
				throw new IllegalArgumentException("Données transmises malformées");
			}
		}
		// ----
		final int mod;
		if (PADDING == coded.charAt(length - OFFSET_2)) {
			mod = OFFSET_2;
		} else if (PADDING == coded.charAt(length - OFFSET_1)) {
			mod = OFFSET_1;
		} else {
			mod = OFFSET_0;
		}
		final int len = length / BASE64_ENCODED_BLOCK_LEN * (BASE64_DECODED_BLOCK_LEN) - mod;
		final int len1 = len - OFFSET_1;
		final int len2 = len - OFFSET_2;
		final byte[] res = new byte[len];
		decodeCharacters(coded, length, len1, len2, res);
		return res;
	}

	private static void decodeCharacters(final String coded, final int length, final int len1, final int len2, final byte[] res) {
		final int[] b = new int[BASE64_ENCODED_BLOCK_LEN];
		int pos = 0;

		for (int i = 0; i < length; i += BASE64_ENCODED_BLOCK_LEN) {
			// on part de i=1 puisque le caractère 0 est la longeur modulo 3 (voir encode)
			for (int j = 0; j < BASE64_ENCODED_BLOCK_LEN; j++) {
				b[j] = DECODE_TABLE[coded.charAt(i + j)];
			}
			res[pos] = (byte) (((b[OFFSET_0] << SHIFT_1BIT) | (b[OFFSET_1] >> SHIFT_2BIT)) & 0xFF);
			if (pos < len1) {
				res[pos + OFFSET_1] = (byte) (((b[OFFSET_1] << SHIFT_2BIT) | (b[OFFSET_2] >> SHIFT_1BIT)) & 0xFF);
				if (pos < len2) {
					res[pos + OFFSET_2] = (byte) (((b[OFFSET_2] << SHIFT_3BIT) | b[OFFSET_3]) & 0xFF);
				}
			}
			pos += BASE64_DECODED_BLOCK_LEN;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String encode(final byte[] raw) {
		Assertion.checkNotNull(raw);
		//-----
		// Encode une série d'octets en base 64.

		final int mod = raw.length % BASE64_DECODED_BLOCK_LEN;
		final int len = raw.length;
		final StringBuilder res = new StringBuilder((len / BASE64_DECODED_BLOCK_LEN + mod == 0 ? 0 : 1) * BASE64_ENCODED_BLOCK_LEN);

		final int[] e = new int[BASE64_DECODED_BLOCK_LEN];
		for (int i = 0; i < len; i += BASE64_DECODED_BLOCK_LEN) {
			e[OFFSET_0] = raw[i] & 0xFF;
			e[OFFSET_1] = ((i + OFFSET_1 < len) ? raw[i + OFFSET_1] : 0) & 0xFF;
			e[OFFSET_2] = ((i + OFFSET_2 < len) ? raw[i + OFFSET_2] : 0) & 0xFF;
			res.append(ENCODE_TABLE[e[OFFSET_0] >> SHIFT_1BIT]);
			res.append(ENCODE_TABLE[((e[OFFSET_0] << SHIFT_2BIT) | (e[OFFSET_1] >> SHIFT_2BIT)) & 0x3F]);
			res.append(i + OFFSET_1 < len ? ENCODE_TABLE[((e[OFFSET_1] << SHIFT_1BIT) | (e[OFFSET_2] >> SHIFT_3BIT)) & 0x3F] : PADDING);
			res.append(i + OFFSET_2 < len ? ENCODE_TABLE[e[OFFSET_2] & 0x3F] : PADDING);
		}
		return res.toString();
	}
}
