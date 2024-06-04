/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
 * Fichier temporaire supprimé automatiquement après utilisation.
 * @author npiedeloup
 */
public final class TempFile {
	/**
	 * Vertigo Temp directory path.
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
	 * Crée un fichier temporaire.
	 * @param prefix Prefix du nom de fichier
	 * @param suffix Suffix du nom de fichier
	 * @param subDirectory Sous-répertoire des fichiers temporaires (null = répertoire temporaire de vertigo = ${java.io.tmpdir}/vertigo/tempFiles)
	 * @throws IOException Exception IO
	 */
	public static File of(final String prefix, final String suffix, final String subDirectory) throws IOException {
		return of(prefix, suffix, Files.createDirectories(VERTIGO_TMP_DIR_PATH.resolve(subDirectory)));
	}

	/**
	 * Crée un fichier temporaire.
	 * @param prefix Prefix du nom de fichier
	 * @param suffix Suffix du nom de fichier
	 * @throws IOException Exception IO
	 */
	public static File of(final String prefix, final String suffix) throws IOException {
		return of(prefix, suffix, Files.createDirectories(VERTIGO_TMP_DIR_PATH));
	}

	private static File of(final String prefix, final String suffix, final Path path) throws IOException {
		final File file = new File(Files.createTempFile(path, prefix, suffix).toAbsolutePath().toString());
		file.deleteOnExit();
		return file;
	}

	/**
	 * We can't use finalize anymore, because we keep a nio.Path reference, so this TempFile ref will be GC anyway we use it in a VFile.
	 * Purge is done by a fileManager's deamon.
	 */
}
