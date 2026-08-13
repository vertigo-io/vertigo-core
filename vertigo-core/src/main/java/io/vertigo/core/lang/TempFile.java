/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
 * Temporary file management with automatic cleanup.
 * Features:
 * - Safe temporary file creation
 * - Dedicated Vertigo temp directory
 * - Automatic deletion on JVM exit
 * - Subdirectory support
 * - Path-based file handling
 * 
 * Files are created in ${java.io.tmpdir}/vertigo/tempFiles
 *
 * @author npiedeloup
 */
public final class TempFile {
	/**
	 * Root directory for Vertigo temporary files.
	 * Created at startup in ${java.io.tmpdir}/vertigo/tempFiles.
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
	 * Creates a temporary file in a subdirectory.
	 *
	 * @param prefix Filename prefix, used to generate unique name
	 * @param suffix Filename extension with dot (e.g. ".tmp")
	 * @param subDirectory Path under temp directory (created if needed)
	 * @return Created temporary file
	 * @throws IOException if file creation fails
	 */
	public static File of(final String prefix, final String suffix, final String subDirectory) throws IOException {
		Assertion.check()
				.isNotBlank(prefix)
				.isNotNull(suffix)
				.isNotBlank(subDirectory);
		//---
		return of(prefix, suffix, Files.createDirectories(VERTIGO_TMP_DIR_PATH.resolve(subDirectory)));
	}

	/**
	 * Creates a temporary file in root temp directory.
	 *
	 * @param prefix Filename prefix, used to generate unique name
	 * @param suffix Filename extension with dot (e.g. ".tmp")
	 * @return Created temporary file
	 * @throws IOException if file creation fails
	 */
	public static File of(final String prefix, final String suffix) throws IOException {
		Assertion.check()
				.isNotBlank(prefix)
				.isNotNull(suffix);
		//---
		return of(prefix, suffix, Files.createDirectories(VERTIGO_TMP_DIR_PATH));
	}

	private static File of(final String prefix, final String suffix, final Path path) throws IOException {
		final File file = Files.createTempFile(path, prefix, suffix).toFile();
		file.deleteOnExit();
		return file;
	}

	/**
	 * Note: Automatic cleanup is now handled by fileManager daemon.
	 * We can't use finalize due to nio.Path reference retention.
	 * TempFile references are garbage collected independently of VFile usage.
	 */
}
