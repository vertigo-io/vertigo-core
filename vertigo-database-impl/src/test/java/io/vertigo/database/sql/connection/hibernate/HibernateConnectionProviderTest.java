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
package io.vertigo.database.sql.connection.hibernate;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.statement.SqlStatement;

@RunWith(JUnitPlatform.class)
public final class HibernateConnectionProviderTest extends AbstractTestCaseJU5 {

	@Inject
	private SqlDataBaseManager sqlDataBaseManager;
	@Inject
	private VTransactionManager transactionManager;

	@Test
	public void ping() throws SQLException {
		final SqlConnectionProvider sqlConnectionProvider = sqlDataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME);
		final String testQuery = sqlConnectionProvider.getDataBase().getSqlDialect().getTestQuery();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final SqlConnection connection = sqlConnectionProvider.obtainConnection();
			try {
				sqlDataBaseManager.executeQuery(
						SqlStatement.builder(testQuery).build(),
						Integer.class, 1,
						connection);
			} finally {
				connection.release();
			}
		}
	}

}
