package io.vertigo.quarto.plugins.publisher.odt;

import io.vertigo.dynamo.file.model.KFile;
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
 * @version $Id: ODTUtil.java,v 1.3 2014/02/27 10:40:50 pchretien Exp $
 */
final class ODTUtil {

	/**
	 * Nom du fichier XML g�rant les contenus.
	 */
	static final String CONTENT_XML = "content.xml";

	/**
	 * Nom du fichier XML g�rant les styles.
	 */
	static final String STYLES_XML = "styles.xml";

	/** Prefix des fichiers temporaires g�n�r�s. */
	private static final String TEMP_FILE_PREFIX = "krep";

	/** Suffix des fichiers temporaires g�n�r�s. */
	private static final String TEMP_FILE_SUFFIX = ".odt";

	/**
	 * Constructeur priv� pour classe utilitaire
	 */
	private ODTUtil() {
		super();
	}

	/**
	 * Extrait le fichier content.xml d'un odt.
	 * .
	 * @param odtFile ZipFile fichier source
	 * @return String contenant le fichier content.xml sous forme de chaine
	 * @throws IOException Si une exception d'entr�e sortie a lieu
	 */
	static String extractContent(final ZipFile odtFile) throws IOException {
		return ZipUtil.readEntry(odtFile, CONTENT_XML);
	}

	/**
	 * Extrait le fichier styles.xml d'un odt.
	 * .
	 * @param odtFile ZipFile fichier source
	 * @return String contenant le fichier styles.xml sous forme de chaine
	 * @throws IOException Si une exception d'entr�e sortie a lieu
	 */
	static String extractStyles(final ZipFile odtFile) throws IOException {
		return ZipUtil.readEntry(odtFile, STYLES_XML);
	}

	/**
	 * Cr�e le fichier content.xml d'un fichier odt par le contenu provenant d'une fusion.
	 * @param odtFile ZipFile d'origine
	 * @param contentXml Contenu du content.xml � remplacer
	 * @param stylesXml Contenu du styles.xml � remplacer
	 * @param newImagesMap Fichiers images � remplacer
	 * @return Fichier fusionn�
	 * @throws IOException Si une IOException a lieu
	 */
	static File createODT(final ZipFile odtFile, final String contentXml, final String stylesXml, final Map<String, KFile> newImagesMap) throws IOException {
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
	 * Teste si un tag est pr�sent dans le tableau de caract�res content � la position index :
	 * �quivalent � : tag.equals(new String(content, index, tag.length())).
	 * @param content Tableau de caract�res
	 * @param index Index o� commenc� la v�rification dans content
	 * @param tag Texte � v�rifier
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
