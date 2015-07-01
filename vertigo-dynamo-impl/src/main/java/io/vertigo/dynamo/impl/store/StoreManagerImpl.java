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
import io.vertigo.dynamo.impl.store.datastore.BrokerConfigImpl;
import io.vertigo.dynamo.impl.store.datastore.BrokerImpl;
import io.vertigo.dynamo.impl.store.datastore.MasterDataConfigImpl;
import io.vertigo.dynamo.impl.store.filestore.FileBrokerConfig;
import io.vertigo.dynamo.impl.store.filestore.FileInfoBrokerImpl;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.impl.store.kvstore.KVDataStorePlugin;
import io.vertigo.dynamo.impl.store.kvstore.KVStoreImpl;
import io.vertigo.dynamo.impl.store.util.BrokerNNImpl;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.BrokerConfig;
import io.vertigo.dynamo.store.datastore.BrokerNN;
import io.vertigo.dynamo.store.datastore.DataStore;
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
	private final BrokerConfigImpl brokerConfig;
	/** Broker des objets métier et des listes. */
	private final DataStore broker;
	private final FileStore fileInfoBroker;
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
		brokerConfig = new BrokerConfigImpl(cacheManager, this, eventManager);
		brokerNN = new BrokerNNImpl(taskManager);
		//---
		//On enregistre le plugin principal du broker : DefaultPhysicalStore
		brokerConfig.getLogicalStoreConfig().registerDefault(defaultStorePlugin);
		broker = new BrokerImpl(brokerConfig);
		//-----
		kvStore = new KVStoreImpl(kvDataStorePlugins);
		//-----
		fileInfoBroker = createFileInfoBroker(fileStorePlugin);
		//-----
		final Map<String, KVDataStorePlugin> map = new HashMap<>();
		for (final KVDataStorePlugin kvDataStorePlugin : kvDataStorePlugins) {
			map.put(kvDataStorePlugin.getDataStoreName(), kvDataStorePlugin);
		}
		kvDataStorePluginBinding = Collections.unmodifiableMap(map);
	}

	private static FileStore createFileInfoBroker(final Option<FileStorePlugin> fileStorePlugin) {
		final FileBrokerConfig fileBrokerConfiguration = new FileBrokerConfig();
		//On enregistre le plugin de gestion des fichiers : facultatif
		if (fileStorePlugin.isDefined()) {
			fileBrokerConfiguration.getLogicalFileStoreConfiguration().registerDefault(fileStorePlugin.get());
		}
		return new FileInfoBrokerImpl(fileBrokerConfiguration);
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
	 * @return Configuration du PersistenceManager
	 */
	@Override
	public BrokerConfig getBrokerConfig() {
		return brokerConfig;
	}

	/** {@inheritDoc} */
	@Override
	public DataStore getDataStore() {
		return broker;
	}

	/** {@inheritDoc} */
	@Override
	public FileStore getFileStore() {
		return fileInfoBroker;
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
