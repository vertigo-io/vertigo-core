package io.vertigo.database;

import io.vertigo.app.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.database.impl.sql.SqlConnectionProviderPlugin;
import io.vertigo.database.impl.sql.SqlDataBaseManagerImpl;
import io.vertigo.database.sql.SqlDataBaseManager;

/**
 * Defines database features.
 *
 * @author mlaroche
 */
public final class DatabaseFeatures extends Features {

	/**
	 * Constructor.
	 */
	public DatabaseFeatures() {
		super("database");
	}

	/**
	 * Add sqlDataBase management to dynamo.
	 * @return  the feature
	 */
	public DatabaseFeatures withSqlDataBase() {
		getModuleConfigBuilder()
				.addComponent(SqlDataBaseManager.class, SqlDataBaseManagerImpl.class);
		return this;
	}

	/**
	 * Add a database connection provider plugin
	 * @param  connectionProviderPluginClass the plugin to use
	 * @param params a list plugin's params
	 * @return the feature
	 */
	public DatabaseFeatures addSqlConnectionProviderPlugin(final Class<? extends SqlConnectionProviderPlugin> connectionProviderPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(connectionProviderPluginClass, params);
		return this;
	}

	/**

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		//
	}
}
