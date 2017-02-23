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
package io.vertigo.dynamo.plugins.database.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import io.vertigo.core.component.ComponentInfo;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.vendor.SqlDataBase;
import io.vertigo.dynamo.impl.database.SqlConnectionProviderPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Describable;
import io.vertigo.util.ListBuilder;

/**
 * Classe de base des fournisseurs de connexions dynamo.
 *
 * @author pchretien
 */
public abstract class AbstractSqlConnectionProviderPlugin implements SqlConnectionProviderPlugin, Describable {

	private static final Logger LOG = Logger.getLogger(AbstractSqlConnectionProviderPlugin.class);

	private final String name;
	/**
	* Base de données utilisée
	*/
	private final SqlDataBase dataBase;

	/**
	 * Constructeur.
	 * @param name ConnectionProvider's name
	 * @param dataBase Type de base de données
	 */
	protected AbstractSqlConnectionProviderPlugin(final String name, final SqlDataBase dataBase) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(dataBase);
		Assertion.checkNotNull(dataBase.getSqlMapping());
		Assertion.checkNotNull(dataBase.getSqlExceptionHandler());
		//-----
		this.name = name;
		this.dataBase = dataBase;
	}

	/** {@inheritDoc} */
	@Override
	public final String getName() {
		return name;
	}

	//=========================================================================
	//-----GESTION DU CONNECTION PROVIDER
	//=========================================================================
	/** {@inheritDoc} */
	@Override
	public final SqlDataBase getDataBase() {
		return dataBase;
	}

	/** {@inheritDoc} */
	@Override
	public final List<ComponentInfo> getInfos() {
		try {
			final SqlConnection connection = obtainConnection();
			try (final Connection jdbcConnection = connection.getJdbcConnection()) {
				try {
					final DatabaseMetaData metaData = jdbcConnection.getMetaData();
					//---
					return new ListBuilder<ComponentInfo>()
							.add(new ComponentInfo("database.name", metaData.getDatabaseProductName()))
							.add(new ComponentInfo("database.version", metaData.getDatabaseProductVersion()))

							.add(new ComponentInfo("database.driver.name", metaData.getDriverName()))
							.add(new ComponentInfo("database.driver.name", metaData.getDriverVersion()))
							.add(new ComponentInfo("database.driver.url", metaData.getURL()))
							.build();
				} finally {
					connection.rollback();
					connection.release();
				}
			}
		} catch (final Exception e) {
			LOG.warn("Can't get database infos", e);
			return Collections.emptyList();
		}
	}
}
