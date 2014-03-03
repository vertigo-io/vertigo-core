package io.vertigo.dynamo.file.model;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;

import java.io.Serializable;

/**
 * Représentation d'un Fichier logique persistant.
 * Ce FileInfo fournit :
 * - le contenu du fichier
 * - son nom d'origine
 * - son type mime
 * - sa taille
 * - sa dernière date de modification

 * @author npiedeloup
 * @version $Id: FileInfo.java,v 1.4 2014/01/20 17:45:43 pchretien Exp $
 */
public interface FileInfo extends Serializable {
	/**
	 * @return Définition de la resource.
	 */
	FileInfoDefinition getDefinition();

	/**
	 * @return URI de la ressource
	 */
	URI<FileInfo> getURI();

	/**
	 * Fixe l'uri de stockage. Cette action n'est possible que si l'URI n'etait pas encore définie.
	 * @param uri uri de stockage, non null.
	 */
	void setURIStored(URI<FileInfo> uri);

	/**
	 * @return Fichier enrichi
	 */
	KFile getKFile();
}
