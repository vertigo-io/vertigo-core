package io.vertigo.dynamo.impl.file.model;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.lang.Assertion;

import java.util.Date;

/**
 * Class générique de définition d'un fichier.
 * @author npiedeloup
 * @version $Id: AbstractKFile.java,v 1.3 2013/10/22 12:33:19 pchretien Exp $
 */
abstract class AbstractKFile implements KFile {
	private static final long serialVersionUID = 1L;
	private final String fileName;
	private final String mimeType;
	private final Date lastModified;
	private final long length;

	/**
	 * Constructeur.
	 * Associe un fichier à des méta-données
	 * @param fileName Nom d'origine du fichier
	 * @param mimeType Type mime du fichier
	 * @param lastModified Date de derniére modification du fichier
	 * @param length Longueur du fichier (en octet)
	 */
	protected AbstractKFile(final String fileName, final String mimeType, final Date lastModified, final long length) {
		Assertion.checkNotNull(fileName);
		Assertion.checkNotNull(mimeType);
		Assertion.checkNotNull(lastModified);
		Assertion.checkNotNull(length);
		//---------------------------------------------------------------------
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.lastModified = lastModified;
		this.length = length;
	}

	/** {@inheritDoc} */
	public final String getFileName() {
		return fileName;
	}

	/** {@inheritDoc} */
	public final String getMimeType() {
		return mimeType;
	}

	/** {@inheritDoc} */
	public final Long getLength() {
		return length;
	}

	/** {@inheritDoc} */
	public final Date getLastModified() {
		return (Date) lastModified.clone();
	}
}
