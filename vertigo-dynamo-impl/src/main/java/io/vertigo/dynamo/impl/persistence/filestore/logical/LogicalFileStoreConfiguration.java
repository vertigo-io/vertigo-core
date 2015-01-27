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
public final class LogicalFileStoreConfiguration {
	/** Store physique par défaut. */
	private FileStore defaultStore;

	/** Map des stores utilisés spécifiquement pour certains DT */
	private final Map<FileInfoDefinition, FileStore> storeMap = new HashMap<>();

	/**
	 * Fournit un store adpaté au type de l'objet.
	 * @param fileInfoDefinition Définition
	 * @return Store utilisé pour cette definition
	 */
	public FileStore getPhysicalStore(final FileInfoDefinition fileInfoDefinition) {
		Assertion.checkNotNull(fileInfoDefinition);
		//-----
		//On regarde si il existe un store enregistré spécifiquement pour cette Definition
		FileStore physicalStore = storeMap.get(fileInfoDefinition);

		physicalStore = physicalStore == null ? defaultStore : physicalStore;
		Assertion.checkNotNull(physicalStore, "Aucun store trouvé pour la définition '{0}'", fileInfoDefinition.getName());
		return physicalStore;
	}

	/**
	 * Enregistre un Store spécifique pour une dtDefinition donnée.
	 * @param definition Définition
	 * @param specificStore Store spécifique
	 */
	public void register(final FileInfoDefinition definition, final FileStore specificStore) {
		//check();
		Assertion.checkNotNull(definition);
		Assertion.checkNotNull(specificStore);
		Assertion.checkArgument(!storeMap.containsKey(definition), "Un store spécifique est déjà enregistré pour cette definition ''{0}'')", storeMap.get(definition));
		//-----
		storeMap.put(definition, specificStore);
	}

	public void registerDefaultPhysicalStore(final FileStore defaultFileStore) {
		Assertion.checkNotNull(defaultFileStore);
		Assertion.checkState(defaultStore == null, "defaultStore deja initialisé");
		//-----
		defaultStore = defaultFileStore;
	}
}
