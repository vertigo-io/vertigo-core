/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
 * Core interface for handling data streams with automatic resource management.
 * Provides a unified approach for:
 * - Converting between stream and byte array representations
 * - Managing stream resources safely
 * - Supporting both reading and buffering operations
 * - Ensuring proper cleanup of system resources
 *
 * Implementations should ensure proper resource cleanup through try-with-resources.
 *
 * @author  pchretien
 */
public interface DataStream {
	/**
	 * Returns the stream content as a byte array.
	 * Automatically manages resource lifecycle:
	 * - Creates input stream
	 * - Buffers content efficiently
	 * - Ensures proper cleanup
	 * Uses a 16KB buffer for optimal performance.
	 *
	 * @return Content as byte array
	 * @throws WrappedException if an I/O error occurs during reading
	 */
	default byte[] getBytes() {
		try (
				final InputStream inputStream = createInputStream();
				final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			int nRead;
			final byte[] data = new byte[16384];
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return buffer.toByteArray();
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	/**
	 * Creates a new input stream for reading the data content.
	 * Each call returns a fresh stream instance that must be closed after use.
	 * Callers should use try-with-resources to ensure proper cleanup.
	 *
	 * @return New input stream instance for reading data
	 * @throws IOException If stream creation fails
	 */
	InputStream createInputStream() throws IOException;
}
