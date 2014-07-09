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
package io.vertigo.dynamo.file.model;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;

import java.io.Serializable;

/**
 * Représentation d'un Fichier logique persistant.
 * Ce FileInfo fournit :
 * - le contenu du fichier
 * - son nom d'origine
 * - son type mime
 * - sa taille
 * - sa dernière date de modification

 * @author npiedeloup
 */
public interface FileInfo extends Serializable {
	/**
	 * @return Définition de la resource.
	 */
	FileInfoDefinition getDefinition();

	/**
	 * @return URI de la ressource
	 */
	URI<FileInfo> getURI();

	/**
	 * Fixe l'uri de stockage. Cette action n'est possible que si l'URI n'etait pas encore définie.
	 * @param uri uri de stockage, non null.
	 */
	void setURIStored(URI<FileInfo> uri);

	/**
	 * @return Fichier enrichi
	 */
	KFile getKFile();
}
