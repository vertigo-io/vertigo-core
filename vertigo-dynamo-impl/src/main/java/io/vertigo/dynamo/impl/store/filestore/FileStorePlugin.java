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
package io.vertigo.dynamo.impl.store.filestore;

import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.lang.Plugin;

/**
 * Plugin de FileStore.
 *
 * @author  npiedeloup
 */
public interface FileStorePlugin extends Plugin {
	/**
	 * Récupération de l'objet correspondant à l'URI fournie.
	 * Peut-être null.
	 *
	 * @param uri FileURI du fichier à charger
	 * @return VFileInfo correspondant à l'URI fournie.
	 */
	FileInfo load(FileInfoURI uri);

	//==========================================================================
	//=============================== Ecriture =================================
	//==========================================================================
	/**
	 * Sauvegarde d'un fichier.
	 * La stratégie de création ou de modification est déduite de l'état de l'objet java,
	 * et notamment de l'état de son URI : new ou stored.
	 *
	 * Si l'objet possède une URI  : mode modification
	 * Si l'objet ne possède pas d'URI : mode création
	 *
	 * @param fileInfo Fichier à sauvegarder (création ou modification)
	 */
	void create(FileInfo fileInfo);

	void update(FileInfo fileInfo);

	/**
	 * Suppression d'un fichier.
	 * @param uri URI du fichier à supprimmer
	 */
	void remove(FileInfoURI uri);
}
