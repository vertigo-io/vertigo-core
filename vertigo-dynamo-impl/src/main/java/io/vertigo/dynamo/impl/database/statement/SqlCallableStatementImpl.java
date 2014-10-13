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

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.database.listener.SqlDataBaseListener;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implémentation standard de la gestion des procédure stockées.
 *
 * @author pchretien
 */
public final class SqlCallableStatementImpl extends SqlPreparedStatementImpl implements SqlCallableStatement {
	/**
	 * Constructeur.
	 *
	 * @param connection Connexion SQL
	 * @param procName Nom de la procédure
	 */
	public SqlCallableStatementImpl(final SqlStatementHandler statementHandler, final SqlDataBaseListener dataBaseListener, final SqlConnection connection, final String procName) {
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
		SqlParameter parameter;
		for (int i = 0; i < getParameters().size(); i++) {
			parameter = getParameter(i);
			if (parameter.isOut()) {
				getCallableStatement().registerOutParameter(i + 1, getConnection().getDataBase().getSqlMapping().getSqlType(parameter.getDataType()));
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
		final SqlParameter parameter = getParameter(index);
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
