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
package io.vertigo.dynamo.impl.database.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracer;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Implémentation Standard de KPrepareStatement.
 *
 * @author pchretien
 */
public final class SqlPreparedStatementImpl implements SqlPreparedStatement {
	private static final int NO_GENERATED_KEY_ERROR_VENDOR_CODE = 100;

	private static final int TOO_MANY_GENERATED_KEY_ERROR_VENDOR_CODE = 464;

	private static final int NULL_GENERATED_KEY_ERROR_VENDOR_CODE = -407;

	private static final int REQUEST_HEADER_FOR_TRACER = 50;

	private static final int FETCH_SIZE = 150;

	/**
	 * Cet objet possède un état interne.
	 * Le fonctionnement du KPreparedStatement est régi par un automate s'appuyant sur ces états.
	 */
	enum State {
		/**
		 *  Registering input/output
		 */
		REGISTERING,

		/**
		 * Etat Exécuté (Permet de lire les données)
		 */
		EXECUTED,

		/**
		 * Etat Avorté (Erreur survenue durant l'exécution de la tache, une exception est automatiquement générée)
		 */
		ABORTED;

		void assertRegisteringState() {
			Assertion.checkArgument(this == State.REGISTERING, "register methods and init() requires REGISTERING state.");
		}

		void assertExecutedState() {
			Assertion.checkArgument(this == State.EXECUTED, "execution was not done successfully");
		}

	}

	private static final int GENERATED_KEYS_INDEX = 1;

	private State state;

	/** Connexion.*/
	private final SqlConnection connection;

	/** PreparedStatement JDBC. */
	private PreparedStatement statement;

	/** Requête SQL. */
	private final String sql;

	/** Si on récupère les clés générées.*/
	private final boolean returnGeneratedKeys;
	private final String[] generatedColumns;

	private final AnalyticsManager analyticsManager;

	//=========================================================================
	//-----GESTION des paramètres types & valeurs
	//=========================================================================
	/**
	 * Listes des paramètres (indexé par les index définis dans les méthodes registerXXX
	 */
	private final List<SqlParameter> parameters = new ArrayList<>();

	/**
	 * Constructor.
	 * @param sql Requête SQL
	 * @param connection Connexion
	 * @param returnGeneratedKeys true si on récupère les clés générées.
	 */
	public SqlPreparedStatementImpl(
			final AnalyticsManager analyticsManager,
			final SqlConnection connection,
			final String sql,
			final boolean returnGeneratedKeys,
			final String... generatedColumns) {
		Assertion.checkNotNull(connection);
		Assertion.checkNotNull(sql);
		Assertion.checkNotNull(analyticsManager);
		//-----
		this.connection = connection;
		this.sql = sql;
		this.returnGeneratedKeys = returnGeneratedKeys;
		this.generatedColumns = generatedColumns;
		//Initialistaion de l'état interne de l'automate
		state = State.REGISTERING;
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc}  */
	@Override
	public final void close() {
		if (statement != null) {
			try {
				statement.close();
			} catch (final SQLException e) {
				throw WrappedException.wrap(e);
			}
		}
	}

	//=========================================================================
	//-----1ere Etape : Enregistrement
	//=========================================================================

	private void init() throws SQLException {
		state.assertRegisteringState();
		//-----
		statement = createStatement();
		//-----
		for (int index = 0; index < parameters.size(); index++) {
			final SqlParameter parameter = parameters.get(index);
			connection.getDataBase().getSqlMapping().setValueOnStatement(statement, index + 1, parameter.getDataType(), parameter.getValue());
		}
	}

	/**
	 * Crée le PreparedStatement JDBC
	 * Cette méthode peut être surchargée pour redéfinir un autre statement (CallableStatement par exemple)
	 *
	 * @throws SQLException Si erreur
	 * @return PreparedStatement JDBC
	 */
	private PreparedStatement createStatement() throws SQLException {
		final PreparedStatement preparedStatement;
		if (generatedColumns.length > 0) {
			preparedStatement = connection.getJdbcConnection().prepareStatement(sql, generatedColumns);
		} else {
			final int autoGeneratedKeys = returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
			preparedStatement = connection.getJdbcConnection().prepareStatement(sql, autoGeneratedKeys);
		}
		preparedStatement.setFetchSize(FETCH_SIZE); //empiriquement 150 est une bonne valeur (Oracle initialise à 10 ce qui est insuffisant)
		return preparedStatement;
	}

	//=========================================================================
	//-----2ème Etape : Setters
	//=========================================================================
	/** {@inheritDoc} */
	@Override
	public final <O> void setValue(final int index, final Class<O> dataType, final O value) throws SQLException {
		state.assertRegisteringState();
		//---
		final SqlParameter parameter = new SqlParameter(dataType, value);
		parameters.add(index, parameter);
	}

	//=========================================================================
	//-----3ème Etape : Exécution
	//=========================================================================

	/** {@inheritDoc} */
	@Override
	public final <O> List<O> executeQuery(final Class<O> dataType, final Integer limit) throws SQLException {
		state.assertRegisteringState();
		Assertion.checkNotNull(dataType);
		//-----
		init();
		//-----
		boolean success = false;
		try {
			final List<O> result = traceWithReturn(tracer -> doExecuteQuery(tracer, dataType, limit));
			success = true;
			return result;
		} catch (final WrappedSqlException e) {
			//SQl Exception is unWrapped
			throw e.getSqlException();
		} finally {
			state = success ? State.EXECUTED : State.ABORTED;
		}
	}

	private <O> List<O> doExecuteQuery(final AnalyticsTracer tracer, final Class<O> dataType, final Integer limit) {
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
	public final int executeUpdate() throws SQLException {
		state.assertRegisteringState();
		//---
		init();
		//---
		boolean success = false;
		try {
			//execution de la Requête
			final int result = traceWithReturn(this::doExecute);
			success = true;
			return result;
		} catch (final WrappedSqlException e) {
			throw e.getSqlException();
		} finally {
			state = success ? State.EXECUTED : State.ABORTED;
		}
	}

	private Integer doExecute(final AnalyticsTracer tracer) {
		try {
			final int res = statement.executeUpdate();
			tracer.setMeasure("nbModifiedRow", res);
			return res;
		} catch (final SQLException e) {
			throw new WrappedSqlException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void addBatch() throws SQLException {
		state.assertRegisteringState();
		//---
		if (statement == null) {
			init();
		}
		//----
		statement.addBatch();
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
	public int executeBatch() throws SQLException {
		state.assertRegisteringState();
		//---
		boolean success = false;
		try {
			final int result = traceWithReturn(this::doExecuteBatch);
			success = true;
			return result;
		} catch (

		final WrappedSqlException e) {
			throw e.getSqlException();
		} finally {
			state = success ? State.EXECUTED : State.ABORTED;
		}
	}

	private Integer doExecuteBatch(final AnalyticsTracer tracer) {
		try {
			final int[] res = statement.executeBatch();
			//Calcul du nombre total de lignes affectées par le batch.
			int count = 0;
			for (final int rowCount : res) {
				count += rowCount;
			}
			tracer.setMeasure("nbModifiedRow", res.length);
			return count;
		} catch (final SQLException e) {
			throw new WrappedSqlException(e);
		}
	}

	/**
	 * Enregistre le début d'exécution du PrepareStatement
	 */
	private <O> O traceWithReturn(final Function<AnalyticsTracer, O> function) {
		return analyticsManager.traceWithReturn(
				"sql",
				"/execute/" + sql.substring(0, Math.min(REQUEST_HEADER_FOR_TRACER, sql.length())),
				tracer -> {
					final O result = function.apply(tracer);
					tracer.addTag("statement", toString());
					return result;
				});
	}

	//=========================================================================
	//-----Utilitaires
	//-----> affichages de la Query  avec ou sans binding pour faciliter le debugging
	//-----> Récupération du statement
	//-----> Récupération de la connection
	//=========================================================================

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return parameters
				.stream()
				.map(SqlParameter::toString)
				.collect(Collectors.joining(", ", sql + '(', ")"));
	}

	/** {@inheritDoc} */
	@Override
	public final <O> O getGeneratedKey(final String columnName, final Class<O> dataType) throws SQLException {
		Assertion.checkArgNotEmpty(columnName);
		Assertion.checkNotNull(dataType);
		Assertion.checkArgument(returnGeneratedKeys || generatedColumns.length > 0, "Statement non créé pour retourner les clés générées");
		state.assertExecutedState();
		//-----
		// L'utilisation des generatedKeys permet d'avoir un seul appel réseau entre le
		// serveur d'application et la base de données pour un insert et la récupération de la
		// valeur de la clé primaire en respectant les standards jdbc et sql ansi.

		// Cela est actuellement utilisé en ms sql server.
		// Cela pourrait à terme être utilisé en Oracle à partir de 10g R2, à condition d'indiquer
		// le nom de la colonne clé primaire lors de la création du PreparedStatement jdbc
		// (et en supprimant la syntaxe propriétaire oracle dans le StoreSQL :
		// begin insert ... returning ... into ... end;)
		// cf http://download-east.oracle.com/docs/cd/B19306_01/java.102/b14355/jdbcvers.htm#CHDEGDHJ
		//code SQLException : http://publib.boulder.ibm.com/infocenter/iseries/v5r3/index.jsp?topic=%2Frzala%2Frzalaco.htm
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
