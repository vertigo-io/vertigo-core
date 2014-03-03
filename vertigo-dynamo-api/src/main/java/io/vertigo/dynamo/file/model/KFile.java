package io.vertigo.dynamo.file.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

/**
 * Représentation d'un Fichier logique.
 * Ce FileInfo fournit :
 * - le contenu du fichier
 * - son nom d'origine
 * - son type mime
 * - sa taille
 * - sa dernière date de modification

 * @author npiedeloup
 * @version $Id: KFile.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface KFile extends Serializable {

	/**
	 * @return Nom d'origine du fichier
	 */
	String getFileName();

	/**
	 * @return Type mime du fichier
	 */
	String getMimeType();

	/**
	 * @return Taille du fichier
	 */
	Long getLength();

	/**
	 * @return Date de modification du fichier en milli-secondes.
	 */
	Date getLastModified();

	/**
	 * @return Stream représentant le document physique.
	 * @throws IOException Erreur d'entrée/sortie
	 */
	InputStream createInputStream() throws IOException;
}
