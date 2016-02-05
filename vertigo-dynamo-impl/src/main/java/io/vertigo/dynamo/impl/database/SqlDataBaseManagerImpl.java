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
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.connection.SqlConnectionProvider;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.impl.database.statement.SqlCallableStatementImpl;
import io.vertigo.dynamo.impl.database.statement.SqlPreparedStatementImpl;
import io.vertigo.dynamo.impl.database.statement.SqlStatementHandler;
import io.vertigo.dynamo.impl.database.statementhandler.SqlStatementHandlerImpl;
import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class SqlDataBaseManagerImpl implements SqlDataBaseManager {
	private final AnalyticsManager analyticsManager;
	private final SqlStatementHandler statementHandler;
	private final Map<String, SqlConnectionProvider> connectionProviderPluginMap;

	/**
	 * Constructeur.
	 * @param localeManager Manager des messages localisés
	 * @param analyticsManager Manager de la performance applicative
	 * @param sqlConnectionProviderPlugins List of connectionProviderPlugin. Names must be unique.
	 */
	@Inject
	public SqlDataBaseManagerImpl(final LocaleManager localeManager, final AnalyticsManager analyticsManager, final List<SqlConnectionProviderPlugin> sqlConnectionProviderPlugins) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkNotNull(analyticsManager);
		Assertion.checkNotNull(sqlConnectionProviderPlugins);
		//-----
		this.analyticsManager = analyticsManager;
		statementHandler = new SqlStatementHandlerImpl();
		connectionProviderPluginMap = new HashMap<>(sqlConnectionProviderPlugins.size());
		for (final SqlConnectionProviderPlugin sqlConnectionProviderPlugin : sqlConnectionProviderPlugins) {
			final String name = sqlConnectionProviderPlugin.getName();
			final SqlConnectionProvider previous = connectionProviderPluginMap.put(name, sqlConnectionProviderPlugin);
			Assertion.checkState(previous == null, "ConnectionProvider {0}, was already registered", name);
		}
		Assertion.checkNotNull(connectionProviderPluginMap.containsKey(MAIN_CONNECTION_PROVIDER_NAME), "No main ConnectionProvider was set. Configure one and only one connectionProviderPlugin with name 'main'.");
		localeManager.add("io.vertigo.dynamo.impl.database.DataBase", io.vertigo.dynamo.impl.database.Resources.values());
	}

	/** {@inheritDoc} */
	@Override
	public SqlConnectionProvider getConnectionProvider(final String name) {
		final SqlConnectionProvider sqlConnectionProvider = connectionProviderPluginMap.get(name);
		Assertion.checkNotNull(sqlConnectionProvider, "ConnectionProvider {0}, wasn't registered.", name);
		return sqlConnectionProvider;
	}

	/** {@inheritDoc} */
	@Override
	public SqlCallableStatement createCallableStatement(final SqlConnection connection, final String procName) {
		//On vérifie la norme des CallableStatement (cf : http://java.sun.com/j2se/1.4.2/docs/api/java/sql/CallableStatement.html)
		//S'il on utilise call, il faut les {..}, sinon les erreurs SQL ne sont pas tout le temps transformées en SQLException (au moins pour oracle)
		Assertion.checkArgument(!procName.contains("call ") || (procName.charAt(0) == '{') && (procName.charAt(procName.length() - 1) == '}'), "Les appels de procédures avec call, doivent être encapsuler avec des {...}, sans cela il y a une anomalie de remonté d'erreur SQL");
		return new SqlCallableStatementImpl(statementHandler, analyticsManager, connection, procName);
	}

	/** {@inheritDoc} */
	@Override
	public SqlPreparedStatement createPreparedStatement(final SqlConnection connection, final String sql, final boolean returnGeneratedKeys) {
		return new SqlPreparedStatementImpl(statementHandler, analyticsManager, connection, sql, returnGeneratedKeys);

	}
}
