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
package io.vertigo.dynamo.plugins.database.connection.c3p0;

import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.vendor.SqlDataBase;
import io.vertigo.dynamo.plugins.database.connection.AbstractSqlConnectionProviderPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ClassUtil;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Implémentation d'un pseudo Pool.
 *
 * @see io.vertigo.dynamo.plugins.database.connection.datasource.DataSourceConnectionProviderPlugin Utiliser une DataSource
 */
public final class C3p0ConnectionProviderPlugin extends AbstractSqlConnectionProviderPlugin {
	private final ComboPooledDataSource pooledDataSource;

	/**
	 * Constructeur (deprecated).
	 * @param name ConnectionProvider's name
	 * @param dataBaseClass Type de base de données
	 * @param jdbcDriver Classe du driver jdbc
	 * @param jdbcUrl URL de configuration jdbc
	 */
	@Inject
	public C3p0ConnectionProviderPlugin(@Named("name") final Option<String> name, @Named("dataBaseClass") final String dataBaseClass, @Named("jdbcDriver") final String jdbcDriver, @Named("jdbcUrl") final String jdbcUrl) {
		super(name.getOrElse(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME), ClassUtil.newInstance(dataBaseClass, SqlDataBase.class));

		Assertion.checkNotNull(jdbcUrl);
		Assertion.checkNotNull(jdbcDriver);
		//-----
		pooledDataSource = new ComboPooledDataSource();
		try {
			pooledDataSource.setDriverClass(jdbcDriver);//loads the jdbc driver
		} catch (final PropertyVetoException e) {
			throw WrappedException.wrapIfNeeded(e, "Can't defined JdbcDriver {0}", jdbcDriver);
		}
		pooledDataSource.setJdbcUrl(jdbcUrl);
		//c3p0 can work with defaults

	}

	/** {@inheritDoc} */
	@Override
	public SqlConnection obtainConnection() throws SQLException {
		return new SqlConnection(pooledDataSource.getConnection(), getDataBase(), true);
	}
}
