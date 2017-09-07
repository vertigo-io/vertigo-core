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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.process.ProcessAnalyticsTracer;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.statement.SqlParameter;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;
import io.vertigo.database.sql.vendor.SqlMapping;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class SqlDataBaseManagerImpl implements SqlDataBaseManager {

	private static final int NO_GENERATED_KEY_ERROR_VENDOR_CODE = 100;

	private static final int TOO_MANY_GENERATED_KEY_ERROR_VENDOR_CODE = 464;

	private static final int NULL_GENERATED_KEY_ERROR_VENDOR_CODE = -407;

	private static final int REQUEST_HEADER_FOR_TRACER = 50;

	private static final int FETCH_SIZE = 150;

	private static final int GENERATED_KEYS_INDEX = 1;

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
	public <O> List<O> executeQuery(
			final SqlStatement sqlStatement,
			final Class<O> dataType,
			final Integer limit,
			final SqlConnection connection) throws SQLException {
		Assertion.checkNotNull(sqlStatement);
		Assertion.checkNotNull(dataType);
		Assertion.checkNotNull(connection);
		//-----
		try (final PreparedStatement statement = createStatement(sqlStatement.getSqlQuery(), connection)) {
			setParameters(sqlStatement.getSqlQuery(), statement, sqlStatement.getSqlParameters(), connection);
			//-----
			return traceWithReturn(sqlStatement.getSqlQuery(), tracer -> doExecuteQuery(statement, tracer, dataType, limit, connection));
		} catch (final WrappedSqlException e) {
			//SQl Exception is unWrapped
			throw e.getSqlException();
		}
	}

	private static <O> List<O> doExecuteQuery(
			final PreparedStatement statement,
			final ProcessAnalyticsTracer tracer,
			final Class<O> dataType,
			final Integer limit,
			final SqlConnection connection) {
		// ResultSet JDBC
		final SqlMapping mapping = connection.getDataBase().getSqlMapping();
		try (final ResultSet resultSet = statement.executeQuery()) {
			//Le Handler a la responsabilité de créer les données.
			final List<O> result = SqlUtil.buildResult(dataType, mapping, resultSet, limit);
			tracer.setMeasure("nbSelectedRow", result.size());
			return result;
		} catch (final SQLException e) {
			//SQl Exception is Wrapped for lambda
			throw new WrappedSqlException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public <O> Tuples.Tuple2<Integer, O> executeUpdateWithGeneratedKey(
			final SqlStatement sqlStatement,
			final GenerationMode generationMode,
			final String columnName,
			final Class<O> dataType,
			final SqlConnection connection) throws SQLException {
		Assertion.checkNotNull(sqlStatement);
		Assertion.checkNotNull(generationMode);
		Assertion.checkNotNull(columnName);
		Assertion.checkNotNull(dataType);
		Assertion.checkNotNull(connection);
		//---
		try (final PreparedStatement statement = createStatement(sqlStatement.getSqlQuery(), generationMode, new String[] { columnName }, connection)) {
			setParameters(sqlStatement.getSqlQuery(), statement, sqlStatement.getSqlParameters(), connection);
			//---
			//execution de la Requête
			final int result = traceWithReturn(sqlStatement.getSqlQuery(), tracer -> doExecute(statement, tracer));
			final O generatedId = getGeneratedKey(statement, columnName, dataType, connection);
			return Tuples.of(result, generatedId);
		} catch (final WrappedSqlException e) {
			throw e.getSqlException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public int executeUpdate(
			final SqlStatement sqlStatement,
			final SqlConnection connection) throws SQLException {
		Assertion.checkNotNull(sqlStatement);
		Assertion.checkNotNull(connection);
		//---
		try (final PreparedStatement statement = createStatement(sqlStatement.getSqlQuery(), connection)) {
			setParameters(sqlStatement.getSqlQuery(), statement, sqlStatement.getSqlParameters(), connection);
			//---
			return traceWithReturn(sqlStatement.getSqlQuery(), tracer -> doExecute(statement, tracer));
		} catch (final WrappedSqlException e) {
			throw e.getSqlException();
		}
	}

	private static int doExecute(final PreparedStatement statement, final ProcessAnalyticsTracer tracer) {
		try {
			final int res = statement.executeUpdate();
			tracer.setMeasure("nbModifiedRow", res);
			return res;
		} catch (final SQLException e) {
			throw new WrappedSqlException(e);
		}
	}

	private static class WrappedSqlException extends RuntimeException {
		private static final long serialVersionUID = -6501399202170153122L;
		private final SQLException sqlException;

		WrappedSqlException(final SQLException sqlException) {
			Assertion.checkNotNull(sqlException);
			//---
			this.sqlException = sqlException;
		}

		SQLException getSqlException() {
			return sqlException;
		}

	}

	/** {@inheritDoc} */
	@Override
	public OptionalInt executeBatch(
			final SqlStatement sqlStatement,
			final SqlConnection connection) throws SQLException {
		Assertion.checkNotNull(sqlStatement);
		Assertion.checkNotNull(connection);
		//---
		try (final PreparedStatement statement = createStatement(sqlStatement.getSqlQuery(), connection)) {
			for (final List<SqlParameter> parameters : sqlStatement.getSqlParametersForBatch()) {
				setParameters(sqlStatement.getSqlQuery(), statement, parameters, connection);
				statement.addBatch();
			}
			return traceWithReturn(sqlStatement.getSqlQuery(), tracer -> doExecuteBatch(statement, tracer));
		} catch (final WrappedSqlException e) {
			throw e.getSqlException();
		}
	}

	private OptionalInt doExecuteBatch(final PreparedStatement statement, final ProcessAnalyticsTracer tracer) {
		try {
			final int[] res = statement.executeBatch();
			//Calcul du nombre total de lignes affectées par le batch.
			int count = 0;
			for (final int rowCount : res) {
				count += rowCount;
				if (rowCount == Statement.SUCCESS_NO_INFO) {
					//if there is only one NO _INFO then we consider that we have no info.
					return OptionalInt.empty();
				}
			}
			tracer.setMeasure("nbModifiedRow", count);
			return OptionalInt.of(count);
		} catch (final SQLException e) {
			throw new WrappedSqlException(e);
		}
	}

	/*
	 * Enregistre le début d'exécution du PrepareStatement
	 */
	private <O> O traceWithReturn(final String sql, final Function<ProcessAnalyticsTracer, O> function) {
		return analyticsManager.traceWithReturn(
				"sql",
				"/execute/" + sql.substring(0, Math.min(REQUEST_HEADER_FOR_TRACER, sql.length())),
				tracer -> {
					final O result = function.apply(tracer);
					tracer.addTag("statement", toString());
					return result;
				});
	}

	private static void setParameters(
			final String sql,
			final PreparedStatement statement,
			final List<SqlParameter> parameters,
			final SqlConnection connection) throws SQLException {
		//-----
		for (int index = 0; index < parameters.size(); index++) {
			final SqlParameter parameter = parameters.get(index);
			connection.getDataBase().getSqlMapping()
					.setValueOnStatement(statement, index + 1, parameter.getDataType(), parameter.getValue());
		}
	}

	//=========================================================================
	//-----Utilitaires
	//-----> affichages de la Query  avec ou sans binding pour faciliter le debugging
	//-----> Récupération du statement
	//-----> Récupération de la connection
	//=========================================================================

	private static PreparedStatement createStatement(final String sql, final SqlConnection connection) throws SQLException {
		final PreparedStatement preparedStatement = connection.getJdbcConnection()
				.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
		//by experience 150 is a right value (Oracle is set by default at 10 : that's not sufficient)
		preparedStatement.setFetchSize(FETCH_SIZE);
		return preparedStatement;
	}

	private static PreparedStatement createStatement(
			final String sql,
			final GenerationMode generationMode,
			final String[] generatedColumns,
			final SqlConnection connection) throws SQLException {
		final PreparedStatement preparedStatement;
		switch (generationMode) {
			case GENERATED_KEYS:
				preparedStatement = connection.getJdbcConnection()
						.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				break;
			case GENERATED_COLUMNS:
				preparedStatement = connection.getJdbcConnection()
						.prepareStatement(sql, generatedColumns);
				break;
			default:
				throw new IllegalStateException();
		}
		//by experience 150 is a right value (Oracle is set by default at 10 : that's not sufficient)
		preparedStatement.setFetchSize(FETCH_SIZE);
		return preparedStatement;
	}

	private static <O> O getGeneratedKey(
			final PreparedStatement statement,
			final String columnName,
			final Class<O> dataType,
			final SqlConnection connection) throws SQLException {
		Assertion.checkArgNotEmpty(columnName);
		Assertion.checkNotNull(dataType);
		//-----
		// L'utilisation des generatedKeys permet d'avoir un seul appel réseau entre le
		// serveur d'application et la base de données pour un insert et la récupération de la
		// valeur de la clé primaire en respectant les standards jdbc et sql ansi.
		try (final ResultSet rs = statement.getGeneratedKeys()) {
			final boolean next = rs.next();
			if (!next) {
				throw new SQLException("GeneratedKeys empty", "02000", NO_GENERATED_KEY_ERROR_VENDOR_CODE);
			}
			final SqlMapping mapping = connection.getDataBase().getSqlMapping();
			//ResultSet haven't correctly named columns so we fall back to get the first column, instead of looking for column index by name.
			final int pkRsCol = GENERATED_KEYS_INDEX;//attention le pkRsCol correspond au n° de column dans le RETURNING
			final O id = mapping.getValueForResultSet(rs, pkRsCol, dataType); //attention le pkRsCol correspond au n° de column dans le RETURNING
			if (rs.wasNull()) {
				throw new SQLException("GeneratedKeys wasNull", "23502", NULL_GENERATED_KEY_ERROR_VENDOR_CODE);
			}

			if (rs.next()) {
				throw new SQLException("GeneratedKeys.size >1 ", "0100E", TOO_MANY_GENERATED_KEY_ERROR_VENDOR_CODE);
			}
			return id;
		}
	}

}
