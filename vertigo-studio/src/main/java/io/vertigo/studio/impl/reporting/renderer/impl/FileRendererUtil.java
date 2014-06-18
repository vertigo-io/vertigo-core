package io.vertigo.studio.impl.reporting.renderer.impl;

import io.vertigo.kernel.exception.VRuntimeException;
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
			throw new VRuntimeException("Erreur IO", e);
		}
	}
}
