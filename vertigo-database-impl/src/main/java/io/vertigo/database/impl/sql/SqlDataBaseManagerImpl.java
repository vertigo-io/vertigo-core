/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.database.impl.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.database.impl.sql.statement.SqlPreparedStatementImpl;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.parser.SqlNamedParam;
import io.vertigo.database.sql.statement.SqlPreparedStatement;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples;
import io.vertigo.lang.Tuples.Tuple2;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class SqlDataBaseManagerImpl implements SqlDataBaseManager {
	private static final char SEPARATOR = '#';
	private final AnalyticsManager analyticsManager;
	private final Map<String, SqlConnectionProvider> connectionProviderPluginMap;

	/**
	 * Constructor.
	 * @param localeManager Manager des messages localisés
	 * @param analyticsManager Manager de la performance applicative
	 * @param sqlConnectionProviderPlugins List of connectionProviderPlugin. Names must be unique.
	 */
	@Inject
	public SqlDataBaseManagerImpl(
			final LocaleManager localeManager,
			final AnalyticsManager analyticsManager,
			final List<SqlConnectionProviderPlugin> sqlConnectionProviderPlugins) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkNotNull(analyticsManager);
		Assertion.checkNotNull(sqlConnectionProviderPlugins);
		//-----
		this.analyticsManager = analyticsManager;
		connectionProviderPluginMap = new HashMap<>(sqlConnectionProviderPlugins.size());
		for (final SqlConnectionProviderPlugin sqlConnectionProviderPlugin : sqlConnectionProviderPlugins) {
			final String name = sqlConnectionProviderPlugin.getName();
			final SqlConnectionProvider previous = connectionProviderPluginMap.put(name, sqlConnectionProviderPlugin);
			Assertion.checkState(previous == null, "ConnectionProvider {0}, was already registered", name);
		}
		localeManager.add("io.vertigo.database.impl.sql.DataBase", io.vertigo.database.impl.sql.Resources.values());
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
	public SqlPreparedStatement createPreparedStatement(final SqlConnection connection) {
		return new SqlPreparedStatementImpl(analyticsManager, connection);
	}

	/** {@inheritDoc} */
	@Override
	public Tuple2<String, List<SqlNamedParam>> parseQuery(final String query) {
		Assertion.checkArgNotEmpty(query);
		//-----
		//we add a space before and after to avoid side effects
		final String[] tokens = (" " + query + " ").split(String.valueOf(SEPARATOR));
		//...#p1#..... => 3 tokens
		//...#p1#.....#p2#... => 5 tokens
		Assertion.checkState(tokens.length % 2 == 1, "a tag is missing on query {0}", query);

		final List<SqlNamedParam> sqlNamedParams = new ArrayList<>();
		final StringBuilder sql = new StringBuilder();
		boolean param = false;
		//request = "select....#param1#... #param2#"
		for (final String token : tokens) {
			if (param) {
				if (token.isEmpty()) {
					//the separator character has been escaped and must be replaced by a single Separator
					sql.append(SEPARATOR);
				} else {
					sqlNamedParams.add(new SqlNamedParam(token));
					sql.append("?");
				}
			} else {
				sql.append(token);
			}
			param = !param;
		}
		//we delete the added spaces...
		sql.delete(0, 1);
		sql.delete(sql.length() - 1, sql.length());

		return Tuples.of(sql.toString(), sqlNamedParams);
	}

}
