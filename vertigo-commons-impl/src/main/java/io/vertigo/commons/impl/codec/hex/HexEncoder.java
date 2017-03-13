/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.impl.codec.hex;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.lang.Assertion;

/**
 * Implémentation threadSafe des mécanismes standards d'encodage/décodage.
 * Hex transforme en chaine de caractères un tableau d'octets
 *
 * @author  pchretirn
 */
public final class HexEncoder implements Encoder<byte[], String> {

	/** {@inheritDoc} */
	@Override
	public String encode(final byte[] data) {
		Assertion.checkNotNull(data);
		//-----
		final StringBuilder output = new StringBuilder(data.length * 2);
		for (final byte element : data) {
			output.append(Integer.toHexString((element >> 4) & 0xf)).append(Integer.toHexString(element & 0xf));
		}
		return output.toString();
	}
}
