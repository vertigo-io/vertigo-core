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
package io.vertigo.dynamo.impl.database;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.database.DataBaseManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.impl.database.listener.SqlDataBaseListener;
import io.vertigo.dynamo.impl.database.listener.SqlDataBaseListenerImpl;
import io.vertigo.dynamo.impl.database.statement.SqlCallableStatementImpl;
import io.vertigo.dynamo.impl.database.statement.SqlPreparedStatementImpl;
import io.vertigo.dynamo.impl.database.statement.SqlStatementHandler;
import io.vertigo.dynamo.impl.database.statementhandler.SqlStatementHandlerImpl;

import javax.inject.Inject;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class DataBaseManagerImpl implements DataBaseManager {
	private final SqlDataBaseListener dataBaseListener;
	private final SqlStatementHandler statementHandler;
	private final SqlConnectionProviderPlugin connectionProviderPlugin;

	/**
	 * Constructeur.
	 * @param localeManager Manager des messages localisés
	 * @param analyticsManager Manager de la performance applicative
	 */
	@Inject
	public DataBaseManagerImpl(final LocaleManager localeManager, final AnalyticsManager analyticsManager, final SqlConnectionProviderPlugin connectionProviderPlugin) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkNotNull(analyticsManager);
		Assertion.checkNotNull(connectionProviderPlugin);
		//---------------------------------------------------------------------
		dataBaseListener = new SqlDataBaseListenerImpl(analyticsManager);
		statementHandler = new SqlStatementHandlerImpl();
		this.connectionProviderPlugin = connectionProviderPlugin;
		localeManager.add("io.vertigo.dynamo.impl.database.DataBase", io.vertigo.dynamo.impl.database.Resources.values());
	}

	/** {@inheritDoc} */
	public SqlConnectionProviderPlugin getConnectionProvider() {
		return connectionProviderPlugin;
	}

	/** {@inheritDoc} */
	public SqlCallableStatement createCallableStatement(final SqlConnection connection, final String procName) {
		return new SqlCallableStatementImpl(statementHandler, dataBaseListener, connection, procName);
	}

	/** {@inheritDoc} */
	public SqlPreparedStatement createPreparedStatement(final SqlConnection connection, final String sql, final boolean returnGeneratedKeys) {
		return new SqlPreparedStatementImpl(statementHandler, dataBaseListener, connection, sql, returnGeneratedKeys);

	}
}
