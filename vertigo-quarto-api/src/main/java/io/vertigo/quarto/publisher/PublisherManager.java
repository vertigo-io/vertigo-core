package io.vertigo.quarto.publisher;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.component.Manager;
import io.vertigo.quarto.publisher.model.PublisherData;

import java.net.URL;

/**
 * Gestionnaire centralis� des �ditions.
 * Le choix du type d'�dition est fait par l'appelant qui fournit les param�tres adapt�s � son besoin.
 *
 * @author pchretien, npiedeloup
 * @version $Id: PublisherManager.java,v 1.4 2014/01/28 18:53:45 pchretien Exp $
 */
public interface PublisherManager extends Manager {
	/** 
	 * Cr�ation d'une nouvelle �dition.
	 * @param fileName Nom du document � g�n�rer (! pas son emplacement de stockage !)
	 * @param modelFileURL Chemin vers le fichier model
	 * @param data Donn�es � fusionner avec le model
	 * @return Tache permettant la production d'un document au format pass� en param�tre
	 */
	KFile publish(String fileName, URL modelFileURL, PublisherData data);

	/** 
	 * Cr�ation asynchrone d'une nouvelle �dition.
	 * @param fileName Nom du document � g�n�rer (! pas son emplacement de stockage !)
	 * @param modelFileURL Chemin vers le fichier model
	 * @param data Donn�es � fusionner avec le model
	 * @param Handler permettant de notifier l'ex�cution de publisher
	 */
	void publishASync(final String fileName, final URL modelFileURL, final PublisherData data, final WorkResultHandler<KFile> workResultHandler);
}
