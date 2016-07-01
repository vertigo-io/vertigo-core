/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.store;

import java.util.List;

import javax.inject.Inject;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.impl.store.datastore.DataStoreConfigImpl;
import io.vertigo.dynamo.impl.store.datastore.DataStoreImpl;
import io.vertigo.dynamo.impl.store.datastore.DataStorePlugin;
import io.vertigo.dynamo.impl.store.datastore.MasterDataConfigImpl;
import io.vertigo.dynamo.impl.store.filestore.FileStoreConfig;
import io.vertigo.dynamo.impl.store.filestore.FileStoreImpl;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.store.datastore.DataStoreConfig;
import io.vertigo.dynamo.store.datastore.MasterDataConfig;
import io.vertigo.dynamo.store.filestore.FileStore;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.lang.Assertion;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class StoreManagerImpl implements StoreManager {
	private final MasterDataConfig masterDataConfig;
	private final DataStoreConfigImpl dataStoreConfig;

	/** DataStore des objets métier et des listes. */
	private final DataStore dataStore;
	private final FileStore fileStore;

	/**
	 * Constructeur.
	 * @param cacheManager Manager de gestion du cache
	 * @param collectionsManager Manager de gestion des collections
	 */
	@Inject
	public StoreManagerImpl(
			final CacheManager cacheManager,
			final VTransactionManager transactionManager,
			final CollectionsManager collectionsManager,
			final List<FileStorePlugin> fileStorePlugins,
			final List<DataStorePlugin> dataStorePlugins,
			final EventBusManager eventBusManager) {
		Assertion.checkNotNull(cacheManager);
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(dataStorePlugins);
		Assertion.checkNotNull(fileStorePlugins);
		Assertion.checkNotNull(eventBusManager);
		//-----
		masterDataConfig = new MasterDataConfigImpl(collectionsManager);
		//---
		//On enregistre le plugin principal du broker
		dataStoreConfig = new DataStoreConfigImpl(dataStorePlugins, cacheManager);
		dataStore = new DataStoreImpl(this, transactionManager, eventBusManager, dataStoreConfig);
		//-----
		final FileStoreConfig fileStoreConfig = new FileStoreConfig(fileStorePlugins);
		fileStore = new FileStoreImpl(fileStoreConfig);
	}

	/** {@inheritDoc} */
	@Override
	public MasterDataConfig getMasterDataConfig() {
		return masterDataConfig;
	}

	/**
	 * @return Configuration du StoreManager
	 */
	@Override
	public DataStoreConfig getDataStoreConfig() {
		return dataStoreConfig;
	}

	/** {@inheritDoc} */
	@Override
	public DataStore getDataStore() {
		return dataStore;
	}

	/** {@inheritDoc} */
	@Override
	public FileStore getFileStore() {
		return fileStore;
	}
}
