package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.statement.KCallableStatement;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.database.DataBaseListener;
import io.vertigo.kernel.lang.Assertion;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implémentation standard de la gestion des procédure stockées.
 *
 * @author pchretien
 * @version $Id: KCallableStatementImpl.java,v 1.4 2014/01/20 17:46:01 pchretien Exp $
 */
public final class KCallableStatementImpl extends KPreparedStatementImpl implements KCallableStatement {
	/**
	 * Constructeur.
	 *
	 * @param connection Connexion SQL
	 * @param procName Nom de la procédure
	 */
	public KCallableStatementImpl(final StatementHandler statementHandler, final DataBaseListener dataBaseListener, final KConnection connection, final String procName) {
		super(statementHandler, dataBaseListener, connection, procName, false);
	}

	//--------------------------------------------------------------------
	//------------------clôture 1ere Etape -------------------------------
	//--------------------------------------------------------------------
	/** {@inheritDoc} */
	@Override
	PreparedStatement createStatement() throws SQLException {
		return getConnection().getJdbcConnection().prepareCall(getSql());
	}

	/** {@inheritDoc} */
	@Override
	void postInit() throws SQLException {
		registerOutParameters();
	}

	/**
	 * Enregistre les paramètres de sortie
	 * @throws SQLException Si erreur
	 */
	private void registerOutParameters() throws SQLException {
		Parameter parameter;
		for (int i = 0; i < getParameters().size(); i++) {
			parameter = getParameter(i);
			if (parameter.isOut()) {
				getCallableStatement().registerOutParameter(i + 1, getConnection().getDataBase().getSqlMapping().getTypeSQL(parameter.getDataType()));
			}
		}
	}

	//--------------------------------------------------------------------
	//------------------3àme Etape : Exécution------------------------------
	//--------------------------------------------------------------------
	//Les méthodes sont définies dans l'ancétre KPrepareStatement
	//Notamment la méthode executeUpdate()

	//--------------------------------------------------------------------
	//------------------4àme Etape : Getters------------------------------
	//--------------------------------------------------------------------

	/** {@inheritDoc} */
	public Object getValue(final int index) throws SQLException {
		Assertion.checkArgument(getState() == State.EXECUTED, "L'exécution n'a pas été effectuée !");
		final Parameter parameter = getParameter(index);
		Assertion.checkArgument(parameter.isOut(), "Les Getters ne peuvent se faire que sur des paramètres OUT");
		//---------------------------------------------------------------------
		//On récupère le type saisi en amont par la méthode register
		final DataType dataType = parameter.getDataType();
		return getConnection().getDataBase().getSqlMapping().getValueForCallableStatement(getCallableStatement(), index + 1, dataType);
	}

	//----------------------------------------------------------------
	//----------------------Utilitaires : affichages de la Query  avec ou sans binding pour faciliter le debugging
	//----------------------------------------------------------------
	/**
	 * Retourne le CallableStatement créé
	 *
	 * @return CallableStatement
	 */
	private CallableStatement getCallableStatement() {
		return (CallableStatement) getPreparedStatement();
	}
}
