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
package io.vertigoimpl.engines.elastica.redis;

import java.io.Serializable;

/**
 * @author pchretien
 */
final class Util {

	private Util() {
		//private
	}

	static ZMethod decodeMethod(final String method) {
		return (ZMethod) doDecode(method);
	}

	static Throwable decodeThrowable(final String hget) {
		return (Throwable) doDecode(hget);
	}

	static Object decodeResult(final String hget) {
		return doDecode(hget);
	}

	static String encodeMethod(final ZMethod method) {
		return doEncode(method);
		//		return codecManager.getBase64Codec().encode(codecManager.getSerializationCodec().encode((Serializable) toEncode));
	}

	private static String doEncode(final Serializable toEncode) {
		//return codecManager.getBase64Codec().encode(codecManager.getSerializationCodec().encode(toEncode));
		throw new UnsupportedOperationException("Pas d'encoder");
	}

	private static Object doDecode(final String encoded) {
		//return codecManager.getSerializationCodec().decode(codecManager.getBase64Codec().decode(encoded));
		throw new UnsupportedOperationException("Pas d'encoder");
	}

	static String encodeResult(final Object result) {
		return doEncode((Serializable) result);
	}

	static String encodeError(final Throwable t) {
		return doEncode(t);
	}

}
