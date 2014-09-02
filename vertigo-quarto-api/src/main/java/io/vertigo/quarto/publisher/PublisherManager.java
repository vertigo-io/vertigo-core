/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.quarto.publisher;

import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.quarto.publisher.model.PublisherData;

import java.net.URL;

/**
 * Gestionnaire centralisé des éditions.
 * Le choix du type d'édition est fait par l'appelant qui fournit les paramètres adaptés à son besoin.
 *
 * @author pchretien, npiedeloup
 */
public interface PublisherManager extends Manager {
	/**
	 * Création d'une nouvelle édition.
	 * @param fileName Nom du document à générer (! pas son emplacement de stockage !)
	 * @param modelFileURL Chemin vers le fichier model
	 * @param data Données à fusionner avec le model
	 * @return Tache permettant la production d'un document au format passé en paramètre
	 */
	KFile publish(String fileName, URL modelFileURL, PublisherData data);

	/**
	 * Création asynchrone d'une nouvelle édition.
	 * @param fileName Nom du document à générer (! pas son emplacement de stockage !)
	 * @param modelFileURL Chemin vers le fichier model
	 * @param data Données à fusionner avec le model
	 * @param Handler permettant de notifier l'exécution de publisher
	 */
	void publishASync(final String fileName, final URL modelFileURL, final PublisherData data, final WorkResultHandler<KFile> workResultHandler);
}
