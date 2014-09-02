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
package io.vertigo.dynamo.plugins.database.connection.datasource;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.vendor.SqlDataBase;
import io.vertigo.dynamo.plugins.database.connection.AbstractConnectionProviderPlugin;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * ConnectionProvider permettant la connexion à une datasource Java.
 *
 * @author alauthier
 */
public final class DataSourceConnectionProviderPlugin extends AbstractConnectionProviderPlugin {
	/**
	 * DataSource
	 */
	private final DataSource dataSource;

	/**
	 * Constructeur.
	 * @param dataBaseName Nom du type de base de données
	 * @param dataSource URL de la dataSource JNDI
	 */
	@Inject
	public DataSourceConnectionProviderPlugin(@Named("classname") final String dataBaseName, @Named("source") final String dataSource) {
		super(createDataBase(dataBaseName));
		Assertion.checkNotNull(dataSource);
		//----------------------------------------------------------------------
		// Initialisation de la source de données
		try {
			final javax.naming.Context context = new javax.naming.InitialContext();
			this.dataSource = (DataSource) context.lookup(dataSource);
		} catch (final NamingException e) {
			throw new RuntimeException("Impossible de récupérer la DataSource", e);
		}
	}

	/** {@inheritDoc} */
	public KConnection obtainConnection() throws SQLException {
		final java.sql.Connection connection = dataSource.getConnection();
		return new KConnection(connection, getDataBase(), true);
	}

	private static SqlDataBase createDataBase(final String dataBaseName) {
		return ClassUtil.newInstance(dataBaseName, SqlDataBase.class);
	}
}
