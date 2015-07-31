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
package io.vertigo.dynamo.file.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilitaire de gestion des fichiers et flux associés.
 *
 * @author npiedeloup
 */
public final class FileUtil {
	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private FileUtil() {
		//rien
	}

	/**
	 * Copie le contenu d'un flux d'entrée vers un flux de sortie.
	 * @param in flux d'entrée
	 * @param out flux de sortie
	 * @throws IOException Erreur d'entrée/sortie
	 */
	public static void copy(final InputStream in, final OutputStream out) throws IOException {
		final int bufferSize = 10 * 1024;
		final byte[] bytes = new byte[bufferSize];
		int read = in.read(bytes);
		while (read != -1) {
			out.write(bytes, 0, read);
			read = in.read(bytes);
		}
	}

	/**
	 * Copie le contenu d'un flux d'entrée vers un fichier de sortie.
	 * @param in flux d'entrée
	 * @param file fichier de sortie
	 * @throws IOException Erreur d'entrée/sortie
	 */
	public static void copy(final InputStream in, final File file) throws IOException {
		try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			FileUtil.copy(in, out);
		}
	}

	/**
	 * Donne l'extension du fichier.
	 * <p>
	 * This method returns the textual part of the filename after the last dot.
	 * There must be no directory separator after the dot.
	 * <pre>
	 * foo.txt      --> "txt"
	 * a/b/c.jpg    --> "jpg"
	 * a/b.txt/c    --> ""
	 * a/b/c        --> ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * @param fileName Nom du fichier
	 *
	 * @return the extension of the file or an empty string if none exists.
	 * (author Apache Commons IO 1.1)
	 */
	public static String getFileExtension(final String fileName) {
		final String extension;
		// The extension separator character.
		final char extensionSeparator = '.';
		// The Unix separator character.
		final char unixSeparator = '/';
		// The Windows separator character.
		final char windowsSeparator = '\\';
		final int extensionPos = fileName.lastIndexOf(extensionSeparator);
		final int lastUnixPos = fileName.lastIndexOf(unixSeparator);
		final int lastWindowsPos = fileName.lastIndexOf(windowsSeparator);
		final int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
		final int index = lastSeparator > extensionPos ? -1 : extensionPos;
		if (index == -1) {
			extension = "";
			// null dans la version cvs précédente
		} else {
			extension = fileName.substring(index + 1).toLowerCase();
		}
		return extension;
	}
}
