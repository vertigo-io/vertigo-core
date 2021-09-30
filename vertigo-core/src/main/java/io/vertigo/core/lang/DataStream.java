/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2021, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Type primitif de Flux.
 *
 * @author  pchretien
 */
public interface DataStream {
	default byte[] getBytes() {
		try (final InputStream inputStream = createInputStream()) {
			try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
				int nRead;
				final byte[] data = new byte[16384];
				while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				return buffer.toByteArray();
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	/**
	 * @return Stream
	 * @throws IOException Erreur d'entrée/sortie
	 */
	InputStream createInputStream() throws IOException;
}
