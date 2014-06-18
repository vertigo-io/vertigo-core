package io.vertigo.dynamo.impl.persistence;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.FileInfo;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage.
 *
 * @author  npiedeloup
 */
public interface FileStore {
	/**
	 * Récupération de l'objet correspondant à l'URI fournie.
	 * Peut-être null.
	 *
	 * @param uri FileURI du fichier à charger
	 * @return KFileInfo correspondant à l'URI fournie.
	 */
	FileInfo load(URI<FileInfo> uri);

	//==========================================================================
	//=============================== Ecriture =================================
	//==========================================================================
	/**
	 * Sauvegarde d'un fichier.
	 * La stratégie de création ou de modification est déduite de l'état de l'objet java,
	 * et notamment de l'état de son URI : new ou stored.
	 *
	 * Si l'objet possède une URI  : mode modification
	 * Si l'objet ne possède pas d'URI : mode création
	 *
	 * @param fileInfo Fichier à sauvegarder (création ou modification)
	 */
	void put(FileInfo fileInfo);

	/**
	 * Suppression d'un fichier.
	 * @param uri URI du fichier à supprimmer
	 */
	void remove(URI<FileInfo> uri);
}
