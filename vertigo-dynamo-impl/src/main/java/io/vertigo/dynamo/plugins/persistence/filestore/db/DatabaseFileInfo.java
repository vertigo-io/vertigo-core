package io.vertigo.dynamo.plugins.persistence.filestore.db;

import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.impl.file.model.AbstractFileInfo;

/**
 * Implémentation par défaut d'un FileInfo issue d'une base de données. 
 * @author npiedeloup
 * @version $Id: DatabaseFileInfo.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
final class DatabaseFileInfo extends AbstractFileInfo {
	private static final long serialVersionUID = -1610176974946554828L;

	/**
	 * Constructeur.
	 * @param fileInfoDefinition Définition du FileInfo
	 * @param kFile Fichier sous jacent
	 */
	protected DatabaseFileInfo(final FileInfoDefinition fileInfoDefinition, final KFile kFile) {
		super(fileInfoDefinition, kFile);
	}
}
