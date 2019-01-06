/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo;

import io.vertigo.app.config.Features;
import io.vertigo.app.config.json.Feature;
import io.vertigo.core.param.Param;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.impl.collections.CollectionsManagerImpl;
import io.vertigo.dynamo.impl.file.FileManagerImpl;
import io.vertigo.dynamo.impl.kvstore.KVStoreManagerImpl;
import io.vertigo.dynamo.impl.kvstore.KVStorePlugin;
import io.vertigo.dynamo.impl.search.SearchManagerImpl;
import io.vertigo.dynamo.impl.store.StoreManagerImpl;
import io.vertigo.dynamo.impl.store.datastore.DataStorePlugin;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.impl.task.TaskManagerImpl;
import io.vertigo.dynamo.kvstore.KVStoreManager;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.TaskManager;

/**
 * Defines dynamo features.
 *
 * @author pchretien
 */
public final class DynamoFeatures extends Features<DynamoFeatures> {

	/**
	 * Constructor.
	 */
	public DynamoFeatures() {
		super("dynamo");
	}

	/**
	 * Add search to dynamo
	 * @return the feature
	 */
	@Feature("search")
	public DynamoFeatures withSearch() {
		getModuleConfigBuilder()
				.addComponent(SearchManager.class, SearchManagerImpl.class);
		return this;
	}

	/**
	 * Add store to dynamo
	 * @return  the feature
	 */
	@Feature("store")
	public DynamoFeatures withStore() {
		getModuleConfigBuilder()
				.addComponent(StoreManager.class, StoreManagerImpl.class);
		return this;
	}

	/**
	 * Add a store plugin
	 * @param dataStorePlugin the plugin to use
	 * @param params a list plugin's params
	 * @return the feature
	 */
	public DynamoFeatures addDataStorePlugin(final Class<? extends DataStorePlugin> dataStorePlugin, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(dataStorePlugin, params);
		return this;
	}

	/**
	 * Add key/value store to dynamo
	 * @return  the feature
	 */
	@Feature("kvStore")
	public DynamoFeatures withKVStore() {
		getModuleConfigBuilder()
				.addComponent(KVStoreManager.class, KVStoreManagerImpl.class);
		return this;
	}

	/**
	 * Add a key/value store plugin
	 * @param  kvStorePlugin the plugin to use
	 * @param params a list plugin's params
	 * @return the feature
	 */
	public DynamoFeatures addKVStorePlugin(final Class<? extends KVStorePlugin> kvStorePlugin, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(kvStorePlugin, params);
		return this;
	}

	/**
	 * Add a plugin to store files
	 * @param  fileStorePluginClass the plugin to use
	 * @param params a list plugin's params
	 * @return the feature
	 */
	public DynamoFeatures addFileStorePlugin(final Class<? extends FileStorePlugin> fileStorePluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(fileStorePluginClass, params);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(CollectionsManager.class, CollectionsManagerImpl.class)
				.addComponent(FileManager.class, FileManagerImpl.class)
				.addComponent(TaskManager.class, TaskManagerImpl.class);

	}
}
