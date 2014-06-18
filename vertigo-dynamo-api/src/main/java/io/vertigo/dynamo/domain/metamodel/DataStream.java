package io.vertigo.dynamo.domain.metamodel;

import java.io.IOException;
import java.io.InputStream;

/**
 * Type primitif de Flux.
 * 
 * @author  pchretien
 */
public interface DataStream {
	/**
	 * @return Flux 
	 * @throws IOException Erreur d'entrée/sortie
	 */
	InputStream createInputStream() throws IOException;

	/**
	 * @return Longueur
	 */
	long getLength();
}