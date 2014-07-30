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
package io.vertigo.studio.plugins.mda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Utilitaire de lecture/écriture des fichiers.
 *
 * @author pchretien
 */
final class FileUtil {

	private static final String EOL = System.getProperty("line.separator");

	/**
	 * Constructeur.
	 */
	private FileUtil() {
		super();
	}

	/**
	 * Ecriture d'un fichier.
	 *
	 * @param file Fichier.
	 * @param content Contenu à écrire
	 * @param encoding encoding du fichier à écrire
	 * @return Si l'écriture s'est bien passée
	 */
	static boolean writeFile(final File file, final String content, final String encoding) {
		try (final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding))) {
			writer.write(content);
			return true;
		} catch (final IOException e) {
			return false;
		}
	}

	/**
	 * Lecture d'un fichier.
	 *
	 * @param file Fichier
	 * @param encoding encoding du fichier à lire
	 * @return Contenu
	 * @throws IOException Erreur d'entrée/sortie
	 */
	static String readContentFile(final File file, final String encoding) throws IOException {
		if (!file.exists()) {
			return null;
		}
		final StringBuilder currentContent = new StringBuilder();
		try (final BufferedReader myReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
			String line = myReader.readLine();
			while (line != null) {
				currentContent.append(line);
				currentContent.append(EOL);
				line = myReader.readLine();
			}
		}
		return currentContent.toString();
	}
}
