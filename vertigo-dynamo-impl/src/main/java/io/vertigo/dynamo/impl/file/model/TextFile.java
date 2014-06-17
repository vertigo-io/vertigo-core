package io.vertigo.dynamo.impl.file.model;

import java.io.InputStream;
import java.util.Date;

/**
 * Fichier construit à partir d'une chaine.
 * Ce TextFile fournit un fichier de type text.
 *
 * @author npiedeloup
 */
public final class TextFile extends AbstractKFile {
	private static final long serialVersionUID = 1L;
	private final String content;

	/**
	 * Constructeur.
	 * Associe un fichier à des méta-données
	 * @param fileName Nom d'origine du fichier
	 * @param content Contenu en lui même	 */
	public TextFile(final String fileName, final String content) {
		this(fileName, "text/plain", content);
	}

	/**
	 * Constructeur.
	 * Associe un fichier à des méta-données
	 * @param fileName Nom d'origine du fichier
	 * @param mimeType Type mime du fichier
	 * @param content Contenu en lui même (non null)
	 */
	public TextFile(final String fileName, final String mimeType, final String content) {
		//le content ne doit pas être null
		super(fileName, mimeType, new Date(), Long.valueOf(content.length()));
		//---------------------------------------------------------------------
		this.content = content;
	}

	/** {@inheritDoc} */
	public InputStream createInputStream() {
		return new java.io.ByteArrayInputStream(content.getBytes());
	}
}
