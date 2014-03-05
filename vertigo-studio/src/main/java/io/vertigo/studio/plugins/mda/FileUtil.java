package io.vertigo.studio.plugins.mda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Utilitaire de lecture/écriture des fichiers.
 * 
 * @author pchretien
 * @version $Id: FileUtil.java,v 1.2 2014/02/27 10:27:53 pchretien Exp $
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
	 * @param file Fichier.
	 * @param content Contenu à écrire 
	 * @return Si l'écriture s'est bien passée
	 */
	static boolean writeFile(final File file, final String content) {
		try (final Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(content);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	/**
	 * Lecture d'un fichier.
	 * @param file Fichier
	 * @return Contenu
	 * @throws IOException Erreur d'entrée/sortie
	 */
	static String readContentFile(final File file) throws IOException {
		if (!file.exists()) {
			return null;
		}
		final StringBuilder currentContent = new StringBuilder();
		try (final BufferedReader myReader = new BufferedReader(new FileReader(file))) {
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
