package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.database.statement.QueryResult;
import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.database.DataBaseListener;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

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
 * @version $Id: KPreparedStatementImpl.java,v 1.9 2014/01/20 17:46:01 pchretien Exp $
 */
public class KPreparedStatementImpl implements KPreparedStatement {
	/**
	 * Cet objet possède un état interne.
	 * Le fonctionnement du KPreparedStatement est régi par un automate s'appuyant sur ces états.
	 */
	static enum State {
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

	private State state = State.UNKNOWN;

	/** Connexion.*/
	private final KConnection connection;

	/** PreparedStatement JDBC. */
	private PreparedStatement statement;

	/** StatementStats du traitement. */
	private final StatementStatsImpl stats;

	/** Requête SQL. */
	private final String sql;

	/** Si on récupère les clés générées.*/
	private final boolean returnGeneratedKeys;

	private final StatementHandler statementHandler;
	private final DataBaseListener dataBaseListener;
	//Début du temps d'exécution
	private long begin;

	//-----------------------------------------------------------------------------------
	//----------------------GESTION des paramètres types & valeurs-----------------------
	//-----------------------------------------------------------------------------------
	/**
	 * Listes des paramètres (indexé par les index définis dans les méthodes registerXXX
	 */
	private final List<Parameter> parameters = new ArrayList<>();

	//--------------------------------------------------------------------
	//------------------Construction / Destruction  -----------------------
	//---------------------------------------------------------------------
	/**
	 * Constructeur.
	 * @param sql Requête SQL
	 * @param connection Connexion
	 * @param returnGeneratedKeys true si on récupère les clés générées.
	 */
	public KPreparedStatementImpl(final StatementHandler statementHandler, final DataBaseListener dataBaseListener, final KConnection connection, final String sql, final boolean returnGeneratedKeys) {
		Assertion.checkNotNull(connection);
		Assertion.checkNotNull(sql);
		Assertion.checkNotNull(statementHandler);
		Assertion.checkNotNull(dataBaseListener);
		//-----------------------------------------------------------------
		this.connection = connection;
		this.sql = sql;
		this.returnGeneratedKeys = returnGeneratedKeys;
		//Initialistaion de l'état interne de l'automate
		state = State.CREATED;
		this.dataBaseListener = dataBaseListener;
		this.statementHandler = statementHandler;
		stats = new StatementStatsImpl(this);
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
	final List<Parameter> getParameters() {
		return parameters;
	}

	/**
	 * Récupération d'un paramètre préalablement enregistré
	 * @param index Indexe du paramètre
	 * @return Valeur du paramètre
	 */
	final Parameter getParameter(final int index) {
		final Parameter p = parameters.get(index);
		Assertion.checkNotNull(p, "Le paramètre à l''index {0} n''a pas été enregistré préalablement !", index);
		return p;
	}

	/** {@inheritDoc}  */
	public final void close() {
		if (statement == null) {
			return;
		}
		//Dans tout les cas on clôture le PS.
		try {
			statement.close();
		} catch (final SQLException e) {
			throw new VRuntimeException(e);
		}
	}

	//--------------------------------------------------------------------
	//------------------1ere Etape : Enregistrement-----------------------
	//--------------------------------------------------------------------
	/**
	 * Ajoute un paramètre en précisant son type 
	 * @param in Si le paramètre est in
	 * @param out Si le paramètre est out
	 * @param index Indexe du paramètre
	 * @param dataType Type Kapser
	 */
	private void registerParameter(final int index, final DataType dataType, final boolean in, final boolean out) {
		Assertion.checkArgument(state == State.CREATED, "L'enregistrement ne peut se faire que sur l'état STATE_CREATED");
		final Parameter parameter = new Parameter(dataType, in, out);
		parameters.add(index, parameter);
	}

	/** {@inheritDoc} */
	public final void registerParameter(final int index, final DataType dataType, final ParameterType parameterType) {
		Assertion.checkNotNull(parameterType);
		//---------------------------------------------------------------------
		switch (parameterType) {
			case IN:
				registerParameter(index, dataType, true, false);
				break;
			case OUT:
				registerParameter(index, dataType, false, true);
				break;
			case INOUT:
				registerParameter(index, dataType, true, true);
				break;
			default:
				throw new IllegalArgumentException("case " + parameterType + " not implemented");
		}
	}

	//--------------------------------------------------------------------
	//------------------Clôture des affectations et 1ere Etape -------------------------------
	//--------------------------------------------------------------------
	/** {@inheritDoc} */
	public final void init() throws SQLException {
		Assertion.checkArgument(state == State.CREATED, "L'enregistrement ne peut se faire que sur l'état STATE_CREATED");
		//----------------------------------------------------------------------
		statement = createStatement();
		//On passe à l'état Défini, l'enregistrement des types  est clôt.
		state = State.DEFINED;
		//---------------------------------
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

	//--------------------------------------------------------------------
	//------------------2ème Etape : Setters------------------------------
	//--------------------------------------------------------------------
	/** {@inheritDoc} */
	public final void setValue(final int index, final Object o) throws SQLException {
		Assertion.checkArgument(state == State.DEFINED, "Les Setters ne peuvent se faire que sur l'état STATE_DEFINED ; une fois les types enregistrés et l'enregistrement clôturé par la méthode init()");
		final Parameter parameter = getParameter(index);
		Assertion.checkArgument(parameter.isIn(), "Les Setters ne peuvent se faire que sur des paramètres IN");

		//On récupère le type saisi en amont par la méthode register
		final DataType dataType = parameter.getDataType();
		connection.getDataBase().getSqlMapping().setValueOnStatement(statement, index + 1, dataType, o);
		//On sauvegarde la valeur du paramètre
		parameter.setValue(o);
	}

	//--------------------------------------------------------------------
	//------------------3ème Etape : Exécution------------------------------
	//--------------------------------------------------------------------

	/** {@inheritDoc} */
	public final QueryResult executeQuery(final Domain domain) throws SQLException {
		Assertion.checkNotNull(domain);
		//---------------------------------------------------------------------
		boolean ok = false;
		beginExecution();
		try {
			// ResultSet JDBC
			final SQLMapping mapping = connection.getDataBase().getSqlMapping();
			final QueryResult result;
			try (final ResultSet resultSet = statement.executeQuery()) {
				//Le Handler a la responsabilité de créer les données.
				result = statementHandler.retrieveData(domain, mapping, resultSet);
				stats.setNbSelectedRow(result.getSQLRowCount());
				ok = true;
				return result;
			}
		} finally {
			endExecution(ok);
		}
	}

	/** {@inheritDoc} */
	public final int executeUpdate() throws SQLException {
		boolean ok = false;
		int res;
		beginExecution();
		try {
			//execution de la Requête
			res = statement.executeUpdate();
			ok = true;
			stats.setNbModifiedRow(res);
		} finally {
			endExecution(ok);
		}
		return res;
	}

	/** {@inheritDoc} */
	public void addBatch() throws SQLException {
		statement.addBatch();
	}

	/** {@inheritDoc} */
	public int executeBatch() throws SQLException {
		boolean ok = false;
		final int[] res;
		beginExecution();
		try {
			res = statement.executeBatch();
			ok = true;
			stats.setNbModifiedRow(res.length);

			//Calcul du nombre total de lignes affectées par le batch.
			int count = 0;
			for (final int rowCount : res) {
				count += rowCount;
			}

			return count;
		} finally {
			endExecution(ok);
		}
	}

	/**
	 * Enregistre le début d'exécution du PrepareStatement
	 */
	private void beginExecution() {
		Assertion.checkArgument(state == State.DEFINED, "L'exécution ne peut se faire que sur l'état STATE_DEFINED ; une fois les types enregistrés, l'enregistrement clôturé par la méthode init() et les valeurs settées");
		dataBaseListener.onPreparedStatementStart(this);
		begin = System.currentTimeMillis();
	}

	/**
	 * Enregistre la fin d'exécution du PrepareStatement
	 * @param ok True si l'exécution s'est effectuée sans erreur
	 */

	private void endExecution(final boolean ok) {
		if (ok) {
			//On passe à l'état exécuté
			state = State.EXECUTED;
		} else {
			state = State.ABORTED;
		}
		stats.setSuccess(ok);
		stats.setElapsedTime(System.currentTimeMillis() - begin);
		dataBaseListener.onPreparedStatementFinish(stats);
	}

	//----------------------------------------------------------------
	//----------Utilitaires
	//----------> affichages de la Query  avec ou sans binding pour faciliter le debugging
	//----------> Récupération du statement
	//----------> Récupération de la connection
	//----------------------------------------------------------------
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
	final KConnection getConnection() {
		return connection;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		final StringBuilder s = new StringBuilder(getSql()).append('(');
		Parameter parameter;
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
				s.append(parameter.getValue().toString());
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
		//----------------------------------------------------------------------
		return statement;
	}

	/** {@inheritDoc} */
	public final Object getGeneratedKey(final String columnName, final Domain domain) throws SQLException {
		Assertion.checkArgNotEmpty(columnName);
		Assertion.checkNotNull(domain);
		Assertion.checkArgument(returnGeneratedKeys, "Statement non créé pour retourner les clés générées");
		Assertion.checkArgument(getState() == State.EXECUTED, "L'exécution n'a pas été effectuée !");
		//---------------------------------------------------------------------
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
			final SQLMapping mapping = connection.getDataBase().getSqlMapping();
			final int pkRsCol = rs.findColumn(columnName);
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
