package io.vertigo.publisher.impl;

import io.vertigo.kernel.component.Plugin;
import io.vertigo.publisher.PublisherFormat;
import io.vertigo.publisher.model.PublisherData;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Merger Plugin pour la fusion de documents.
 * 
 * @author adufranne
 * @version $Id: MergerPlugin.java,v 1.2 2013/10/22 12:07:11 pchretien Exp $
 */
public interface MergerPlugin extends Plugin {

	/**
	 * Point d'entr�e du plugin.
	 * 
	 * @param modelFileURL Chemin vers le fichier model
	 * @param data Donn�es � fusionner avec le model
	 * @return le File g�n�r�.
	 * @throws IOException en cas d'erreur de lecture ou d'�criture.
	 */
	File execute(final URL modelFileURL, final PublisherData data) throws IOException;

	/**
	 * @return Type de format g�r� par ce plugin
	 */
	PublisherFormat getPublisherFormat();

}
