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
package io.vertigo.dynamo.database;

import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.database.connection.ConnectionProvider;
import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.statement.KCallableStatement;
import io.vertigo.dynamo.database.statement.KPreparedStatement;

/**
* Gestionnaire des accès aux bases de données.
*
* @author pchretien
*/
public interface DataBaseManager extends Manager {
	/**
	 * @return ConnectionProvider
	 */
	ConnectionProvider getConnectionProvider();

	/**
	 * @param connection Connexion
	 * @param procName  Nom de la procédure
	 * @return statement
	 */
	KCallableStatement createCallableStatement(final KConnection connection, final String procName);

	/**
	 * @param connection Connexion
	 * @param sql Requête SQL
	 * @param generatedKeys Si on récupère les clés générées par la base de données.
	 * @return Statement
	 */
	KPreparedStatement createPreparedStatement(final KConnection connection, final String sql, final boolean generatedKeys);

}
