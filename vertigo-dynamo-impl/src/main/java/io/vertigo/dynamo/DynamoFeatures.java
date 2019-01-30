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
import io.vertigo.dynamo.impl.search.SearchManagerImpl;
import io.vertigo.dynamo.impl.store.StoreManagerImpl;
import io.vertigo.dynamo.impl.task.TaskManagerImpl;
import io.vertigo.dynamo.kvstore.KVStoreManager;
import io.vertigo.dynamo.plugins.collections.lucene.LuceneIndexPlugin;
import io.vertigo.dynamo.plugins.kvstore.berkeley.BerkeleyKVStorePlugin;
import io.vertigo.dynamo.plugins.kvstore.delayedmemory.DelayedMemoryKVStorePlugin;
import io.vertigo.dynamo.plugins.search.elasticsearch.embedded.ESEmbeddedSearchServicesPlugin;
import io.vertigo.dynamo.plugins.search.elasticsearch.transport.ESTransportSearchServicesPlugin;
import io.vertigo.dynamo.plugins.store.datastore.sql.SqlDataStorePlugin;
import io.vertigo.dynamo.plugins.store.filestore.db.DbFileStorePlugin;
import io.vertigo.dynamo.plugins.store.filestore.fs.FsFileStorePlugin;
import io.vertigo.dynamo.plugins.store.filestore.fs.FsFullFileStorePlugin;
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
	 * Add key/value store to dynamo
	 * @return  the feature
	 */
	@Feature("kvStore")
	public DynamoFeatures withKVStore() {
		getModuleConfigBuilder()
				.addComponent(KVStoreManager.class, KVStoreManagerImpl.class);
		return this;
	}

	@Feature("sqlStore")
	public DynamoFeatures withSqlStore(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(SqlDataStorePlugin.class, params);
		return this;
	}

	@Feature("esEmbedded")
	public DynamoFeatures withESEmbedded(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(ESEmbeddedSearchServicesPlugin.class, params);
		return this;
	}

	@Feature("esTransport")
	public DynamoFeatures withESTransport(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(ESTransportSearchServicesPlugin.class, params);
		return this;
	}

	@Feature("berkeleyKV")
	public DynamoFeatures withBerkleyKV(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(BerkeleyKVStorePlugin.class, params);
		return this;
	}

	@Feature("delayedMemoryKV")
	public DynamoFeatures withDelayedMemoryKV(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(DelayedMemoryKVStorePlugin.class, params);
		return this;
	}

	@Feature("fsFile")
	public DynamoFeatures withFsFileStore(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(FsFileStorePlugin.class, params);
		return this;
	}

	@Feature("dbFile")
	public DynamoFeatures withDbFileStore(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(DbFileStorePlugin.class, params);
		return this;
	}

	@Feature("fsFullFile")
	public DynamoFeatures withFsFullFileStore(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(FsFullFileStorePlugin.class, params);
		return this;
	}

	@Feature("luceneIndex")
	public DynamoFeatures withLuceneIndex(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(LuceneIndexPlugin.class, params);
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