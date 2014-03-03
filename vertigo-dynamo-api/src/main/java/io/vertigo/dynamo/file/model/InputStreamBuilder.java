package io.vertigo.dynamo.file.model;

import java.io.IOException;
import java.io.InputStream;

/**
 * Builder d'inputStream pour les données d'un FileInfo.
 * @author npiedeloup
 * @version $Id: InputStreamBuilder.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface InputStreamBuilder {
	/**
	 * @return Stream fournissant les données du document.
	 * @throws IOException Erreur d'entrée/sortie
	 */
	InputStream createInputStream() throws IOException;
}
