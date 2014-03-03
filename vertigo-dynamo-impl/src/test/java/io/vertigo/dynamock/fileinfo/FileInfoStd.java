package io.vertigo.dynamock.fileinfo;

import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.impl.file.model.AbstractFileInfo;

/**
 * Attention cette classe est générée automatiquement !
 * Objet représentant un fichier persistant FileInfoStd
 */
public final class FileInfoStd extends AbstractFileInfo {
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur par défaut.
	 * @param kFile Données du fichier
	 */
	public FileInfoStd(final KFile kFile) {
		super(FileInfoDefinition.findFileInfoDefinition(FileInfoStd.class), kFile);
	}
}
