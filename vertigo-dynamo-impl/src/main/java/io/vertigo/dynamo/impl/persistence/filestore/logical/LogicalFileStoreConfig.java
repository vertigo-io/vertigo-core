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
package io.vertigo.dynamo.impl.persistence.filestore.logical;

import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.impl.persistence.filestore.FileStore;
import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration logique des stores physiques.
 * @author pchretien, npiedeloup
 */
public final class LogicalFileStoreConfig {
	/** Store physique par défaut. */
	private FileStore defaultFileStore;

	/** Map des stores utilisés spécifiquement pour certains DT */
	private final Map<FileInfoDefinition, FileStore> fileStores = new HashMap<>();

	/**
	 * Fournit un store adpaté au type de l'objet.
	 * @param fileInfoDefinition Définition
	 * @return Store utilisé pour cette definition
	 */
	public FileStore getPhysicalFileStore(final FileInfoDefinition fileInfoDefinition) {
		Assertion.checkNotNull(fileInfoDefinition);
		//-----
		//On regarde si il existe un store enregistré spécifiquement pour cette Definition
		FileStore physicalStore = fileStores.get(fileInfoDefinition);

		physicalStore = physicalStore == null ? defaultFileStore : physicalStore;
		Assertion.checkNotNull(physicalStore, "Aucun store trouvé pour la définition '{0}'", fileInfoDefinition.getName());
		return physicalStore;
	}

	/**
	 * Enregistre un Store spécifique pour une dtDefinition donnée.
	 * @param definition Définition
	 * @param fileStore Store spécifique
	 */
	public void register(final FileInfoDefinition definition, final FileStore fileStore) {
		//check();
		Assertion.checkNotNull(definition);
		Assertion.checkNotNull(fileStore);
		Assertion.checkArgument(!fileStores.containsKey(definition), "A fileStore is already bound to this definition '{0}')", fileStores.get(definition));
		//-----
		fileStores.put(definition, fileStore);
	}

	public void registerDefault(final FileStore fileStore) {
		Assertion.checkNotNull(fileStore);
		Assertion.checkState(defaultFileStore == null, "defaultFileStore is alreadey defined");
		//-----
		defaultFileStore = fileStore;
	}
}
