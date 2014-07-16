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
package io.vertigo.studio.impl.reporting.renderer.impl;

import io.vertigo.kernel.lang.Assertion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe utilitaire.
 * 
 * @author tchassagnette
 */
final class FileRendererUtil {

	private FileRendererUtil() {
		//Vide.
	}

	/**
	 * Méthode d'écriture d'un fichier.
	 * @param fileName Nom du fichier
	 * @param content Contenu du fichier.
	 */
	static void writeFile(final String rootPath, final String fileName, final String content) {
		Assertion.checkArgument(rootPath.endsWith("/"), "Le chemin doit se terminer par / ({0})", rootPath);
		//---------------------------------------------------------------------
		final File pathFile = new File(rootPath);
		try {
			if (!pathFile.exists()) {
				if (!pathFile.mkdirs()) {
					throw new IOException("Can't create directory: " + pathFile.getAbsolutePath());
				}
			}
			final File file = new File(rootPath + "" + fileName);

			//On crée un fichier si il n'en existe pas déjà un
			//Atomically creates a new, empty file named by this abstract pathname if and only if a file with this name does not yet exis
			file.createNewFile();

			try (final FileWriter fileWriter = new FileWriter(file)) {
				fileWriter.write(content);
			}
			//System.out.println("writing" + file.getCanonicalPath());
		} catch (final IOException e) {
			throw new RuntimeException("Erreur IO", e);
		}
	}
}
