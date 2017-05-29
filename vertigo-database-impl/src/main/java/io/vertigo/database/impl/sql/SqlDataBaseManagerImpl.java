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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.database.impl.sql.parser.SqlParserHandler;
import io.vertigo.database.impl.sql.statement.SqlPreparedStatementImpl;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.parser.SqlNamedParam;
import io.vertigo.database.sql.statement.SqlPreparedStatement;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;
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
	/**
	 * Liste des séparateurs utilisés dans le traitement des requêtes KSP.
	 */
	private static final ScriptSeparator SQL_SEPARATOR = new ScriptSeparator(SEPARATOR);
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
	public SqlPreparedStatement createPreparedStatement(
			final SqlConnection connection,
			final String sql,
			final GenerationMode generationMode,
			final String... generatedColumns) {
		Assertion.checkNotNull(connection);
		Assertion.checkNotNull(sql);
		Assertion.checkNotNull(generationMode);
		Assertion.checkNotNull(generatedColumns);
		Assertion.when(generationMode != GenerationMode.GENERATED_COLUMNS)
				.check(() -> generatedColumns.length == 0, "generated columns are expected only when mode='GENERATED_COLUMNS'");
		Assertion.when(generationMode == GenerationMode.GENERATED_COLUMNS)
				.check(() -> generatedColumns.length > 0, "generated columns are expected only when mode='GENERATED_COLUMNS'");
		//---
		final boolean returnGeneratedKeys = generationMode == GenerationMode.GENERATED_KEYS;
		return new SqlPreparedStatementImpl(analyticsManager, connection, sql, returnGeneratedKeys, generatedColumns);
	}

	/** {@inheritDoc} */
	@Override
	public Tuple2<String, List<SqlNamedParam>> parseQuery(final String query, final ScriptManager scriptManager) {
		Assertion.checkArgNotEmpty(query);
		//-----
		final SqlParserHandler scriptHandler = new SqlParserHandler();
		scriptManager.parse(query, scriptHandler, Collections.singletonList(SQL_SEPARATOR));
		return Tuples.of(scriptHandler.getSql(), scriptHandler.getParams());
	}

}
