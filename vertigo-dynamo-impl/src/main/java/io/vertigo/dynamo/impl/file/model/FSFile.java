package io.vertigo.dynamo.impl.file.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Représentation d'un fichier créé à partir d'un FileSystem.
 *
 * @author npiedeloup
 */
public final class FSFile extends AbstractKFile {
	private static final long serialVersionUID = 1L;
	private final File file;

	/**
	 * Constructeur.
	 * Associe un fichier à des méta-données
	 * @param fileName Nom d'origine du fichier
	 * @param mimeType Type mime du fichier
	 * @param file Fichier en lui même (non null)
	 */
	public FSFile(final String fileName, final String mimeType, final File file) {
		super(fileName, mimeType, new Date(file.lastModified()), Long.valueOf(file.length()));
		//---------------------------------------------------------------------
		this.file = file;
	}

	/**
	 * @return Fichier en lui même	 */
	public File getFile() {
		return file;
	}

	/** {@inheritDoc} */
	public InputStream createInputStream() throws IOException {
		return new java.io.FileInputStream(file);
		//Exemple de code où on recrée à chaque fois le inputStream
		//return new java.io.FileInputStream(file);

		//Exemple de code où on bufferise la première fois puis on le reset.
		//		if (inputStream == null) {
		//			inputStream = new BufferedInputStream(new java.io.FileInputStream(file));
		//			inputStream.mark(Integer.MAX_VALUE);
		//		} else {
		//			inputStream.reset();
		//		}
		//return inputStream;
	}

}
