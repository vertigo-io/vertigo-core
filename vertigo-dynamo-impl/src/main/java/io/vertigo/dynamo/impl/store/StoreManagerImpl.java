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
package io.vertigo.dynamo.impl.store;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.event.EventManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.impl.store.datastore.DataStoreConfigImpl;
import io.vertigo.dynamo.impl.store.datastore.DataStoreImpl;
import io.vertigo.dynamo.impl.store.datastore.MasterDataConfigImpl;
import io.vertigo.dynamo.impl.store.filestore.FileStoreConfig;
import io.vertigo.dynamo.impl.store.filestore.FileStoreImpl;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.impl.store.kvstore.KVDataStorePlugin;
import io.vertigo.dynamo.impl.store.kvstore.KVStoreImpl;
import io.vertigo.dynamo.impl.store.util.BrokerNNImpl;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.BrokerNN;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.store.datastore.DataStoreConfig;
import io.vertigo.dynamo.store.datastore.DataStorePlugin;
import io.vertigo.dynamo.store.datastore.MasterDataConfig;
import io.vertigo.dynamo.store.filestore.FileStore;
import io.vertigo.dynamo.store.kvstore.KVStore;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
	private final BrokerNN brokerNN;
	private final KVStore kvStore;

	private final Map<String, KVDataStorePlugin> kvDataStorePluginBinding;

	/**
	 * Constructeur.
	 * @param cacheManager Manager de gestion du cache
	 * @param collectionsManager Manager de gestion des collections
	 */
	@Inject
	public StoreManagerImpl(final TaskManager taskManager,
			final CacheManager cacheManager,
			final CollectionsManager collectionsManager,
			final Option<FileStorePlugin> fileStorePlugin,
			final DataStorePlugin defaultStorePlugin,
			final List<KVDataStorePlugin> kvDataStorePlugins,
			final EventManager eventManager) {
		Assertion.checkNotNull(taskManager);
		Assertion.checkNotNull(cacheManager);
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(fileStorePlugin);
		Assertion.checkNotNull(defaultStorePlugin);
		Assertion.checkNotNull(kvDataStorePlugins);
		Assertion.checkNotNull(eventManager);
		//-----
		masterDataConfig = new MasterDataConfigImpl(collectionsManager);
		dataStoreConfig = new DataStoreConfigImpl(cacheManager, this, eventManager);
		brokerNN = new BrokerNNImpl(taskManager);
		//---
		//On enregistre le plugin principal du broker : DefaultPhysicalStore
		dataStoreConfig.getLogicalStoreConfig().registerDefault(defaultStorePlugin);
		dataStore = new DataStoreImpl(dataStoreConfig);
		//-----
		kvStore = new KVStoreImpl(kvDataStorePlugins);
		//-----
		fileStore = createFileIStore(fileStorePlugin);
		//-----
		final Map<String, KVDataStorePlugin> map = new HashMap<>();
		for (final KVDataStorePlugin kvDataStorePlugin : kvDataStorePlugins) {
			map.put(kvDataStorePlugin.getDataStoreName(), kvDataStorePlugin);
		}
		kvDataStorePluginBinding = Collections.unmodifiableMap(map);
	}

	private static FileStore createFileIStore(final Option<FileStorePlugin> fileStorePlugin) {
		final FileStoreConfig fileStoreConfig = new FileStoreConfig();
		//On enregistre le plugin de gestion des fichiers : facultatif
		if (fileStorePlugin.isDefined()) {
			fileStoreConfig.getLogicalFileStoreConfiguration().registerDefault(fileStorePlugin.get());
		}
		return new FileStoreImpl(fileStoreConfig);
	}

	@Override
	public KVStore getKVStore() {
		return kvStore;
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

	@Override
	public BrokerNN getBrokerNN() {
		return brokerNN;
	}

	public KVDataStorePlugin getKVDataStorePlugin(final String dataStoreName) {
		final KVDataStorePlugin kvDataStorePlugin = kvDataStorePluginBinding.get(dataStoreName);
		//-----
		Assertion.checkNotNull(kvDataStorePlugin, "No KVDataStorePlugin bind to this name : {0}. Registered dataStoreNames : ({1})", dataStoreName, kvDataStorePluginBinding.keySet());
		//-----
		return kvDataStorePlugin;
	}
}
