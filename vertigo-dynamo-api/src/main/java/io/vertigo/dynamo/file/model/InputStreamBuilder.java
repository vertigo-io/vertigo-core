package io.vertigo.dynamo.file.model;

import java.io.IOException;
import java.io.InputStream;

/**
 * Builder d'inputStream pour les données d'un FileInfo.
 * @author npiedeloup
 */
public interface InputStreamBuilder {
	/**
	 * @return Stream fournissant les données du document.
	 * @throws IOException Erreur d'entrée/sortie
	 */
	InputStream createInputStream() throws IOException;
}
