/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
