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
package io.vertigo.quarto.publisher.impl.merger.processor;

import io.vertigo.kernel.lang.Assertion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Classe d'utilitaires pour les fichiers de type Zip.
 *
 * @author pforhan
 */
public final class ZipUtil {

	/**
	 * Encoder UTF8.
	 */
	private static final String ENCODER = "UTF-8";

	/**
	 * Taille du Buffer.
	 */
	private static final int BUFFER_SIZE = 8 * 1024;

	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private ZipUtil() {
		super();
	}

	/**
	 * Lecture d'un fichier du fichier ODT.
	 *
	 * @param odtFile ZipFile Fichier source
	 * @param entryName Nom de l'entrée à extraire
	 * @return String le contenu du fichier sous forme de chaine encodée avec ENCODER
	 * @throws IOException Si une exception d'entrée-sortie de fichier a lieu
	 */
	public static String readEntry(final ZipFile odtFile, final String entryName) throws IOException {
		Assertion.checkNotNull(odtFile);
		Assertion.checkArgNotEmpty(entryName);
		final ZipEntry zipEntry = odtFile.getEntry(entryName);
		Assertion.checkNotNull(zipEntry, "Le modèle {0} ne contient pas {1}, vérifier que le modèle est un document valide et du bon type.", odtFile.getName(), entryName);
		//---------------------------------------------------------------------
		final StringBuilder resultat = new StringBuilder();

		try (final InputStreamReader reader = new InputStreamReader(odtFile.getInputStream(zipEntry), ENCODER)) {
			final char[] buffer = new char[BUFFER_SIZE];
			int len;
			while ((len = reader.read(buffer, 0, BUFFER_SIZE)) > 0) {
				resultat.append(buffer, 0, len);
			}
		}
		return resultat.toString();
	}

	/**
	 * Ecriture d'une entry dans le fichier Zip à partir de son contenu et de son nom sous formes de chaine.
	 * .
	 * @param outputZipFile ZipOutputStream Fichier à modifier
	 * @param entryContent Contenu de l'entry à insérer
	 * @param entryName Nom de l'entry
	 * @throws IOException Si une exception d'entrée sortie a lieu
	 */
	public static void writeEntry(final ZipOutputStream outputZipFile, final String entryContent, final String entryName) throws IOException {
		final ZipEntry content = new ZipEntry(entryName);
		outputZipFile.putNextEntry(content);
		final OutputStreamWriter writer = new OutputStreamWriter(outputZipFile, ENCODER);
		writer.write(entryContent, 0, entryContent.length());
		writer.flush();
	}

	/**
	 * Ecriture d'une entry dans le fichier Zip à partir de son contenu et de son nom sous formes de chaine.
	 * .
	 * @param outputZipFile ZipOutputStream Fichier à modifier
	 * @param entryContent Flux de l'entry à insérer
	 * @param entryName Nom de l'entry
	 * @throws IOException Si une exception d'entrée sortie a lieu
	 */
	public static void writeEntry(final ZipOutputStream outputZipFile, final InputStream entryContent, final String entryName) throws IOException {
		writeEntry(outputZipFile, entryContent, new ZipEntry(entryName));
	}

	/**
	 * Ecriture d'une entry dans le fichier Zip à partir de son contenu et de son nom sous formes de chaine.
	 * .
	 * @param outputOdtFile ZipOutputStream Fichier à modifier
	 * @param entryContent Flux de l'entry à insérer
	 * @param zipEntry ZipEntry
	 * @throws IOException Si une exception d'entrée sortie a lieu
	 */
	public static void writeEntry(final ZipOutputStream outputOdtFile, final InputStream entryContent, final ZipEntry zipEntry) throws IOException {
		outputOdtFile.putNextEntry(zipEntry);

		final int bufferSize = 10 * 1024;
		final byte[] bytes = new byte[bufferSize];
		int read;
		while ((read = entryContent.read(bytes)) > 0) {
			outputOdtFile.write(bytes, 0, read);
		}
		outputOdtFile.flush();
	}
}
