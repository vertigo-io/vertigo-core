package io.vertigo.dynamo.domain.metamodel;

import java.io.IOException;
import java.io.InputStream;

/**
 * Type primitif de Flux.
 * 
 * @author  pchretien
 * @version $Id: DataStream.java,v 1.2 2013/07/29 11:43:52 pchretien Exp $
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