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
package io.vertigo.dynamo.export;

import io.vertigo.core.spaces.component.Manager;
import io.vertigo.dynamo.export.model.Export;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.work.WorkResultHandler;

/**
 * Gestionnaire centralisé des éditions de données.
 * Le choix du type de report est fait par l'appelant qui fournit les paramètres adaptés à son besoin.
 *
 * @author pchretien, npiedeloup
 */
public interface ExportManager extends Manager {

	/**
	 * Création du fichier d'export
	 * @param export Expotr à envoyer
	 * @return Fichier
	 */
	KFile createExportFile(final Export export);

	/**
	 * Création asynchrone du fichier d'export
	 * @param export Expotr à envoyer
	 * @param workResultHandler Handler du resultat
	 */
	void createExportFileASync(final Export export, final WorkResultHandler<KFile> workResultHandler);
}
