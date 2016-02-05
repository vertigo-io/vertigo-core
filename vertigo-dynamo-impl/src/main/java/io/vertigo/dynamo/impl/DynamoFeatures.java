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
package io.vertigo.dynamo.impl;

import io.vertigo.app.config.Features;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.impl.collections.CollectionsManagerImpl;
import io.vertigo.dynamo.impl.database.SqlConnectionProviderPlugin;
import io.vertigo.dynamo.impl.database.SqlDataBaseManagerImpl;
import io.vertigo.dynamo.impl.file.FileManagerImpl;
import io.vertigo.dynamo.impl.search.SearchManagerImpl;
import io.vertigo.dynamo.impl.search.SearchServicesPlugin;
import io.vertigo.dynamo.impl.store.StoreManagerImpl;
import io.vertigo.dynamo.impl.task.TaskManagerImpl;
import io.vertigo.dynamo.impl.transaction.VTransactionAspect;
import io.vertigo.dynamo.impl.transaction.VTransactionManagerImpl;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.transaction.VTransactionManager;

/**
 * Defines dynamo features.
 * 
 * @author pchretien
 */
public final class DynamoFeatures extends Features {

	/**
	 * Constructor.
	 */
	public DynamoFeatures() {
		super("dynamo");
	}

	/** {@inheritDoc} */
	@Override
	protected void setUp() {
		getModuleConfigBuilder()
				.addComponent(CollectionsManager.class, CollectionsManagerImpl.class)
				.addComponent(FileManager.class, FileManagerImpl.class)
				.addComponent(TaskManager.class, TaskManagerImpl.class)
				.addComponent(VTransactionManager.class, VTransactionManagerImpl.class)
				.addAspect(VTransactionAspect.class);
	}

	public DynamoFeatures withStore() {
		getModuleConfigBuilder()
				.addComponent(StoreManager.class, StoreManagerImpl.class);
		return this;
	}

	public DynamoFeatures withSQL(final Class<SqlConnectionProviderPlugin> connectionProviderPluginClass) {
		getModuleConfigBuilder()
				.addComponent(SqlDataBaseManager.class, SqlDataBaseManagerImpl.class)
				.addPlugin(connectionProviderPluginClass);
		return this;
	}

	public DynamoFeatures withSearch(final Class<SearchServicesPlugin> searchServicesPluginClass) {
		getModuleConfigBuilder()
				.addComponent(SearchManager.class, SearchManagerImpl.class)
				.addPlugin(searchServicesPluginClass);
		return this;
	}
}
