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
package io.vertigo.dynamo.persistence.filestore;

import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.file.model.FileInfo;

/**
 * Un objet est automatiquement géré par le broker.
 * Les méthodes de mises à jour lacent des erreurs utilisateurs et techniques.
 * Les méthodes d'accès aux données ne lancent que des erreurs techniques.
 *
 * @author  pchretien
 */
public interface FileInfoBroker {

	/**
	 * @param fileInfo File to save
	 * @deprecated Use create or update instead. Will be removed in next version.
	 */
	@Deprecated
	void save(FileInfo fileInfo);

	/**
	 * Create a new File.
	 *
	 * @param fileInfo File to create
	 */
	void create(FileInfo fileInfo);

	/**
	 * Update  an existing File.
	 *
	 * @param fileInfo File to update
	 */
	void update(FileInfo fileInfo);

	/**
	 * Suppression d'un fichier.
	 * @param uri URI du fichier à supprimmer
	 */
	void deleteFileInfo(FileInfoURI uri);

	/**
	 * Récupération d'un fichier par son URI.
	 *
	 * @param uri FileURI du fichier à charger
	 * @return VFileInfo correspondant à l'URI fournie.
	 */
	FileInfo getFileInfo(final FileInfoURI uri);
}
