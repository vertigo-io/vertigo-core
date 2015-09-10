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
package io.vertigo.dynamox.task;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.lang.Assertion;

import java.sql.SQLException;

import javax.inject.Inject;

/**
 * Permet l'appel de requête de manipulation de données (insert, update, delete)
 * ou de procédures stockées. Une tache utilisant ce provider ne traite pas les
 * DtList.<br>
 * <br>
 * Paramètres d'entrée : n String, Integer, Date, Boolean, ByteArray ou DtObject<br>
 * Paramètres de sortie : n String, Integer, Date, Boolean, ByteArray ou DtObject<br>
 * Paramètres d'entrée/sortie : n String, Integer, Date, Boolean, ByteArray ou DtObject<br>
 * <br>
 * Les paramètres de type DtObject ne peuvent pas être null.<br>
 * <br>
 * Chaine de configuration :<br>
 * La chaine de configuration utilise les délimiteurs #NOM# pour les paramètres IN,
 * %NOM% pour les paramètres OUT et @NOM@ pour les paramètres INOUT. L'utilisation
 * d'une valeur d'un DtObject est déclarée par #DTOBJECT.FIELD#, @DTOBJECT.FIELD@
 * ou %DTOBJECT.FIELD% de manière indépendant de la déclaration du mode d'entrée/sortie
 * pour le DtObject. Ainsi, un DtObject déclaré en IN peut voir un de ses champs utilisé
 * en paramètre OUT.<br>
 * Si un paramètre out ou in/out INT_SQL_ROWCOUNT est défini, il reçoit le résultat de executeUpdate.
 *
 * @author  FCONSTANTIN
 */
public class TaskEngineProc extends AbstractTaskEngineSQL<SqlCallableStatement> {

	/**
	 * Constructeur.
	 * @param scriptManager Manager de traitment de scripts
	 */
	@Inject
	public TaskEngineProc(final ScriptManager scriptManager, final VTransactionManager transactionManager, final SqlDataBaseManager sqlDataBaseManager) {
		super(scriptManager, transactionManager, sqlDataBaseManager);
	}

	/** {@inheritDoc} */
	@Override
	protected final void checkSqlQuery(final String sql) {
		//On vérifie la norme des CallableStatement (cf : http://java.sun.com/j2se/1.4.2/docs/api/java/sql/CallableStatement.html)
		//S'il on utilise call, il faut les {..}, sinon les erreurs SQL ne sont pas tout le temps transformées en SQLException (au moins pour oracle)
		Assertion.checkArgument(!sql.contains("call ") || sql.charAt(0) == '{' && sql.charAt(sql.length() - 1) == '}', "Les appels de procédures avec call, doivent être encapsuler avec des {...}, sans cela il y a une anomalie de remonté d'erreur SQL");
	}

	/** {@inheritDoc} */
	@Override
	protected int doExecute(final SqlConnection connection, final SqlCallableStatement statement) throws SQLException {
		setInParameters(statement);
		final int sqlRowcount = statement.executeUpdate();
		setOutParameter(statement);
		return sqlRowcount;
	}

	/** {@inheritDoc} */
	@Override
	protected final SqlCallableStatement createStatement(final String procName, final SqlConnection connection) {
		return getDataBaseManager().createCallableStatement(connection, procName);
	}
}
