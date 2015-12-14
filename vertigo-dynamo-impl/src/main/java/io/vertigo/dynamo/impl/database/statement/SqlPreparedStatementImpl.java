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
package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.database.statement.SqlQueryResult;
import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.impl.database.listener.SqlDataBaseListener;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation Standard de KPrepareStatement.
 *
 * @author pchretien
 */
public class SqlPreparedStatementImpl implements SqlPreparedStatement {
	/**
	 * Cet objet possède un état interne.
	 * Le fonctionnement du KPreparedStatement est régi par un automate s'appuyant sur ces états.
	 */
	enum State {
		/**
		 *  Etat inconnu (Le prepareStatement n'est pas encore initialisé)
		 */
		UNKNOWN,

		/**
		 *  Etat Créé (Permet d'enregistrer les entrées/sorties)
		 */
		CREATED,

		/**
		 * Etat Défini (Clôture la phase d'enregistrement, permet d'exévuter une requête)
		 */
		DEFINED,

		/**
		 * Etat Exécuté (Permet de lire les données)
		 */
		EXECUTED,

		/**
		 * Etat Avorté (Erreur survenue durant l'exécution de la tache, une exception est automatiquement générée)
		 */
		ABORTED
	}

	private static final int GENERATED_KEYS_INDEX = 1;

	private State state = State.UNKNOWN;

	/** Connexion.*/
	private final SqlConnection connection;

	/** PreparedStatement JDBC. */
	private PreparedStatement statement;

	/** Requête SQL. */
	private final String sql;

	/** Si on récupère les clés générées.*/
	private final boolean returnGeneratedKeys;

	private final SqlStatementHandler statementHandler;
	private final SqlDataBaseListener dataBaseListener;
	//Début du temps d'exécution
	private long begin;

	//=========================================================================
	//-----GESTION des paramètres types & valeurs
	//=========================================================================
	/**
	 * Listes des paramètres (indexé par les index définis dans les méthodes registerXXX
	 */
	private final List<SqlParameter> parameters = new ArrayList<>();

	/**
	 * Constructeur.
	 * @param sql Requête SQL
	 * @param connection Connexion
	 * @param returnGeneratedKeys true si on récupère les clés générées.
	 */
	public SqlPreparedStatementImpl(final SqlStatementHandler statementHandler, final SqlDataBaseListener dataBaseListener, final SqlConnection connection, final String sql, final boolean returnGeneratedKeys) {
		Assertion.checkNotNull(connection);
		Assertion.checkNotNull(sql);
		Assertion.checkNotNull(statementHandler);
		Assertion.checkNotNull(dataBaseListener);
		//-----
		this.connection = connection;
		this.sql = sql;
		this.returnGeneratedKeys = returnGeneratedKeys;
		//Initialistaion de l'état interne de l'automate
		state = State.CREATED;
		this.dataBaseListener = dataBaseListener;
		this.statementHandler = statementHandler;
	}

	/**
	 * Retourne l'état de l'automate
	 * @return Etat de l'automate
	 */
	final State getState() {
		return state;
	}

	/**
	 * Récupération de la liste des paramètres
	 * @return Liste des paramètres
	 */
	final List<SqlParameter> getParameters() {
		return parameters;
	}

	/**
	 * Récupération d'un paramètre préalablement enregistré
	 * @param index Indexe du paramètre
	 * @return Valeur du paramètre
	 */
	final SqlParameter getParameter(final int index) {
		final SqlParameter p = parameters.get(index);
		Assertion.checkNotNull(p, "Le paramètre à l''index {0} n''a pas été enregistré préalablement !", index);
		return p;
	}

	/** {@inheritDoc}  */
	@Override
	public final void close() {
		if (statement != null) {
			try {
				statement.close();
			} catch (final SQLException e) {
				throw new WrappedException(e);
			}
		}
	}

	//=========================================================================
	//-----1ere Etape : Enregistrement
	//=========================================================================
	/** {@inheritDoc} */
	@Override
	public final void registerParameter(final int index, final DataType dataType, final boolean in) {
		Assertion.checkArgument(state == State.CREATED, "L'enregistrement ne peut se faire que sur l'état STATE_CREATED");
		final SqlParameter parameter = new SqlParameter(dataType, in);
		parameters.add(index, parameter);
	}

	//=========================================================================
	//-----Clôture des affectations et 1ere Etape
	//=========================================================================
	/** {@inheritDoc} */
	@Override
	public final void init() throws SQLException {
		Assertion.checkArgument(state == State.CREATED, "L'enregistrement ne peut se faire que sur l'état STATE_CREATED");
		//-----
		statement = createStatement();
		//On passe à l'état Défini, l'enregistrement des types  est clôt.
		state = State.DEFINED;
		//-----
		postInit();
	}

	/**
	 * Permet d'enregistrer les variables OUT dans le cas du callableStatement.
	 *
	 * @throws SQLException Si erreur lors de la construction
	 */
	void postInit() throws SQLException {
		//Ne fait rien dans le cas du preparestatement.
	}

	/**
	 * Crée le PreparedStatement JDBC
	 * Cette méthode peut être surchargée pour redéfinir un autre statement (CallableStatement par exemple)
	 *
	 * @throws SQLException Si erreur
	 * @return PreparedStatement JDBC
	 */
	PreparedStatement createStatement() throws SQLException {
		final PreparedStatement preparedStatement;
		if (returnGeneratedKeys) {
			preparedStatement = connection.getJdbcConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		} else {
			preparedStatement = connection.getJdbcConnection().prepareStatement(sql);
		}
		preparedStatement.setFetchSize(150); //empiriquement 150 est une bonne valeur (Oracle initialise à 10 ce qui est insuffisant)
		return preparedStatement;
	}

	//=========================================================================
	//-----2ème Etape : Setters
	//=========================================================================
	/** {@inheritDoc} */
	@Override
	public final void setValue(final int index, final Object o) throws SQLException {
		Assertion.checkArgument(state == State.DEFINED, "Les Setters ne peuvent se faire que sur l'état STATE_DEFINED ; une fois les types enregistrés et l'enregistrement clôturé par la méthode init()");
		final SqlParameter parameter = getParameter(index);
		Assertion.checkArgument(parameter.isIn(), "Les Setters ne peuvent se faire que sur des paramètres IN");

		//On récupère le type saisi en amont par la méthode register
		final DataType dataType = parameter.getDataType();
		connection.getDataBase().getSqlMapping().setValueOnStatement(statement, index + 1, dataType, o);
		//On sauvegarde la valeur du paramètre
		parameter.setValue(o);
	}

	//=========================================================================
	//-----3ème Etape : Exécution
	//=========================================================================

	/** {@inheritDoc} */
	@Override
	public final SqlQueryResult executeQuery(final Domain domain) throws SQLException {
		Assertion.checkNotNull(domain);
		//-----
		boolean ok = false;
		beginExecution();
		Integer nbSelectedRow = null;
		try {
			// ResultSet JDBC
			final SqlMapping mapping = connection.getDataBase().getSqlMapping();
			try (final ResultSet resultSet = statement.executeQuery()) {
				//Le Handler a la responsabilité de créer les données.
				final SqlQueryResult result = statementHandler.retrieveData(domain, mapping, resultSet);
				nbSelectedRow = result.getSQLRowCount();
				ok = true;
				return result;
			}
		} finally {
			endExecution(ok, null, nbSelectedRow);
		}
	}

	/** {@inheritDoc} */
	@Override
	public final int executeUpdate() throws SQLException {
		boolean ok = false;
		Integer nbModifiedRow = null;
		beginExecution();
		try {
			//execution de la Requête
			final int res = statement.executeUpdate();
			ok = true;
			nbModifiedRow = res;
			return res;
		} finally {
			endExecution(ok, nbModifiedRow, null);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void addBatch() throws SQLException {
		statement.addBatch();
	}

	/** {@inheritDoc} */
	@Override
	public int executeBatch() throws SQLException {
		boolean ok = false;
		Integer nbModifiedRow = null;
		beginExecution();
		try {
			final int[] res = statement.executeBatch();
			ok = true;
			nbModifiedRow = res.length;

			//Calcul du nombre total de lignes affectées par le batch.
			int count = 0;
			for (final int rowCount : res) {
				count += rowCount;
			}

			return count;
		} finally {
			endExecution(ok, nbModifiedRow, null);
		}
	}

	/**
	 * Enregistre le début d'exécution du PrepareStatement
	 */
	private void beginExecution() {
		Assertion.checkArgument(state == State.DEFINED, "L'exécution ne peut se faire que sur l'état STATE_DEFINED ; une fois les types enregistrés, l'enregistrement clôturé par la méthode init() et les valeurs settées");
		dataBaseListener.onStart(toString());
		begin = System.currentTimeMillis();
	}

	/**
	 * Enregistre la fin d'exécution du PrepareStatement
	 * @param success True si l'exécution s'est effectuée sans erreur
	 */
	private void endExecution(final boolean success, final Integer nbModifiedRow, final Integer nbSelectedRow) {
		if (success) {
			//On passe à l'état exécuté
			state = State.EXECUTED;
		} else {
			state = State.ABORTED;
		}
		final long elapsedTime = System.currentTimeMillis() - begin;
		dataBaseListener.onFinish(
				toString(),
				success,
				elapsedTime,
				nbModifiedRow,
				nbSelectedRow);
	}

	//=========================================================================
	//-----Utilitaires
	//-----> affichages de la Query  avec ou sans binding pour faciliter le debugging
	//-----> Récupération du statement
	//-----> Récupération de la connection
	//=========================================================================

	/**
	 * Retourne la chaine SQL de la requête.
	 * @return Chaine SQL de la Requête
	 */
	final String getSql() {
		return sql;
	}

	/**
	 * Retourne la connexion utilisée
	 * @return Connexion utilisée
	 */
	final SqlConnection getConnection() {
		return connection;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		final StringBuilder s = new StringBuilder(getSql()).append('(');
		SqlParameter parameter;
		for (int i = 0; i < getParameters().size(); i++) {
			parameter = getParameter(i);
			if (i > 0) {
				s.append(", ");
			}
			if (parameter.isIn()) {
				s.append("in");
			}
			if (parameter.isOut()) {
				s.append("out");
			}
			s.append('=');
			if (parameter.getValue() != null) {
				s.append(parameter.getValue());
			} else {
				s.append("null");
			}
		}
		s.append(')');
		return s.toString();
	}

	/**
	 * Retourne le preparedStatement
	 *
	 * @return PreparedStatement
	 */
	final java.sql.PreparedStatement getPreparedStatement() {
		Assertion.checkNotNull(statement, "Le statement est null, l'exécution est elle OK ?");
		//-----
		return statement;
	}

	/** {@inheritDoc} */
	@Override
	public final Object getGeneratedKey(final String columnName, final Domain domain) throws SQLException {
		Assertion.checkArgNotEmpty(columnName);
		Assertion.checkNotNull(domain);
		Assertion.checkArgument(returnGeneratedKeys, "Statement non créé pour retourner les clés générées");
		Assertion.checkArgument(getState() == State.EXECUTED, "L'exécution n'a pas été effectuée !");
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
				throw new SQLException("GeneratedKeys empty", "02000", 100);
			}
			final SqlMapping mapping = connection.getDataBase().getSqlMapping();
			//ResultSet haven't correctly named columns so we fall back to get the first column, instead of looking for column index by name.
			final int pkRsCol = GENERATED_KEYS_INDEX;//attention le pkRsCol correspond au n° de column dans le RETURNING
			final Object key = mapping.getValueForResultSet(rs, pkRsCol, domain.getDataType()); //attention le pkRsCol correspond au n° de column dans le RETURNING
			if (rs.wasNull()) {
				throw new SQLException("GeneratedKeys wasNull", "23502", -407);
			}

			if (rs.next()) {
				throw new SQLException("GeneratedKeys.size >1 ", "0100E", 464);
			}
			return key;
		}
	}
}
