package io.vertigo.dynamo.impl;

import io.vertigo.core.config.Features;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.export.ExportManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.impl.collections.CollectionsManagerImpl;
import io.vertigo.dynamo.impl.database.SqlConnectionProviderPlugin;
import io.vertigo.dynamo.impl.database.SqlDataBaseManagerImpl;
import io.vertigo.dynamo.impl.export.ExportManagerImpl;
import io.vertigo.dynamo.impl.file.FileManagerImpl;
import io.vertigo.dynamo.impl.search.SearchManagerImpl;
import io.vertigo.dynamo.impl.search.SearchServicesPlugin;
import io.vertigo.dynamo.impl.store.StoreManagerImpl;
import io.vertigo.dynamo.impl.task.TaskManagerImpl;
import io.vertigo.dynamo.impl.transaction.VTransactionManagerImpl;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.transaction.VTransactionManager;

public final class DynamoFeatures extends Features {

	public DynamoFeatures() {
		super("dynamo");
	}

	@Override
	protected void setUp() {
		getModuleConfigBuilder()
				.addComponent(CollectionsManager.class, CollectionsManagerImpl.class)
				.addComponent(ExportManager.class, ExportManagerImpl.class)
				.addComponent(FileManager.class, FileManagerImpl.class)
				.addComponent(TaskManager.class, TaskManagerImpl.class)
				.addComponent(VTransactionManager.class, VTransactionManagerImpl.class);
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
