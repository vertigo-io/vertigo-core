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
package io.vertigo.quarto.plugins.publisher.odt;

import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.quarto.publisher.impl.merger.processor.ZipUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Classe d'utilitaires pour les fichiers de type ODT.
 *
 * @author pforhan
 */
final class ODTUtil {

	/**
	 * Nom du fichier XML gérant les contenus.
	 */
	static final String CONTENT_XML = "content.xml";

	/**
	 * Nom du fichier XML gérant les styles.
	 */
	static final String STYLES_XML = "styles.xml";

	/** Prefix des fichiers temporaires générés. */
	private static final String TEMP_FILE_PREFIX = "quarto";

	/** Suffix des fichiers temporaires générés. */
	private static final String TEMP_FILE_SUFFIX = ".odt";

	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private ODTUtil() {
		super();
	}

	/**
	 * Extrait le fichier content.xml d'un odt.
	 * .
	 * @param odtFile ZipFile fichier source
	 * @return String contenant le fichier content.xml sous forme de chaine
	 * @throws IOException Si une exception d'entrée sortie a lieu
	 */
	static String extractContent(final ZipFile odtFile) throws IOException {
		return ZipUtil.readEntry(odtFile, CONTENT_XML);
	}

	/**
	 * Extrait le fichier styles.xml d'un odt.
	 * .
	 * @param odtFile ZipFile fichier source
	 * @return String contenant le fichier styles.xml sous forme de chaine
	 * @throws IOException Si une exception d'entrée sortie a lieu
	 */
	static String extractStyles(final ZipFile odtFile) throws IOException {
		return ZipUtil.readEntry(odtFile, STYLES_XML);
	}

	/**
	 * Crée le fichier content.xml d'un fichier odt par le contenu provenant d'une fusion.
	 * @param odtFile ZipFile d'origine
	 * @param contentXml Contenu du content.xml à remplacer
	 * @param stylesXml Contenu du styles.xml à remplacer
	 * @param newImagesMap Fichiers images à remplacer
	 * @return Fichier fusionné
	 * @throws IOException Si une IOException a lieu
	 */
	static File createODT(final ZipFile odtFile, final String contentXml, final String stylesXml, final Map<String, VFile> newImagesMap) throws IOException {
		final File resultFile = new TempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
		try (final ZipOutputStream outputFichierOdt = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(resultFile)))) {
			for (final ZipEntry zipEntry : Collections.list(odtFile.entries())) {
				final String entryName = zipEntry.getName();
				if (newImagesMap.containsKey(entryName)) {

					try (final InputStream imageIS = newImagesMap.get(entryName).createInputStream()) {
						ZipUtil.writeEntry(outputFichierOdt, imageIS, entryName);
					}
				} else if (CONTENT_XML.equals(entryName)) {
					ZipUtil.writeEntry(outputFichierOdt, contentXml, CONTENT_XML);
				} else if (STYLES_XML.equals(entryName)) {
					ZipUtil.writeEntry(outputFichierOdt, stylesXml, STYLES_XML);
				} else {

					try (final InputStream zipIS = odtFile.getInputStream(zipEntry)) {
						ZipUtil.writeEntry(outputFichierOdt, zipIS, zipEntry);
					}
				}
				outputFichierOdt.closeEntry();
			}
		}
		return resultFile;
	}

	/**
	 * Teste si un tag est présent dans le tableau de caractères content à la position index :
	 * équivalent à : tag.equals(new String(content, index, tag.length())).
	 * @param content Tableau de caractères
	 * @param index Index où commencé la vérification dans content
	 * @param tag Texte à vérifier
	 * @return boolean
	 */
	public static boolean regionMatches(final char[] content, final int index, final String tag) {
		final int length = tag.length();
		for (int i = 0; i < length; i++) {
			if (content[i + index] != tag.charAt(i)) {
				return false;
			}
		}
		return true;
	}
}
