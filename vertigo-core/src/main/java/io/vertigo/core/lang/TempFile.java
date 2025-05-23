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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Temporary file management utility.
 * Provides a safe way to create and handle temporary files with automatic cleanup.
 * Files are created in a dedicated Vertigo temporary directory and are automatically
 * deleted when the JVM exits.
 *
 * @author npiedeloup
 */
public final class TempFile {
	/**
	 * Vertigo temporary directory path.
	 */
	public static final Path VERTIGO_TMP_DIR_PATH;
	static {
		try {
			final Path vertigoTmpDir = Paths.get(System.getProperty("java.io.tmpdir"), "/vertigo/tempFiles");
			Files.createDirectories(vertigoTmpDir);
			VERTIGO_TMP_DIR_PATH = vertigoTmpDir.toAbsolutePath();
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	/**
	 * Creates a temporary file.
	 *
	 * @param prefix Filename prefix
	 * @param suffix Filename suffix
	 * @param subDirectory Temporary files subdirectory (null = vertigo temp directory = ${java.io.tmpdir}/vertigo/tempFiles)
	 * @throws IOException IO Exception
	 */
	public static File of(final String prefix, final String suffix, final String subDirectory) throws IOException {
		return of(prefix, suffix, Files.createDirectories(VERTIGO_TMP_DIR_PATH.resolve(subDirectory)));
	}

	/**
	 * Creates a temporary file.
	 *
	 * @param prefix Filename prefix
	 * @param suffix Filename suffix
	 * @throws IOException IO Exception
	 */
	public static File of(final String prefix, final String suffix) throws IOException {
		return of(prefix, suffix, Files.createDirectories(VERTIGO_TMP_DIR_PATH));
	}

	private static File of(final String prefix, final String suffix, final Path path) throws IOException {
		final File file = Files.createTempFile(path, prefix, suffix).toFile();
		file.deleteOnExit();
		return file;
	}

	/**
	 * We can't use finalize anymore as we keep a nio.Path reference.
	 * TempFile reference will be GC regardless of VFile usage.
	 * Purge is handled by fileManager daemon.
	 */
}
