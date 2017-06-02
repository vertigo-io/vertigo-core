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
package io.vertigo.database.sql.statement;

import java.sql.SQLException;
import java.util.List;

/**
 * PreparedStatement.
 *
 * Il s'agit d'une encapsulation du preparedStatement Java
 * On peut ainsi tracer toutes les exécution de requêtes
 * On peut aussi débugger les requêtes en listant les paramètres en entrée : ce qui n'est pas possible sur preparedStatement de base.
 *
 *
 * @author pchretien
 */
public interface SqlPreparedStatement extends AutoCloseable {
	/**
	 * Executes a sql query returning a list
	 *
	 * @param sqlParameters input params
	 * @param dataType the return dataType of the list
	 * @param limit the return limit (null if no limit)
	 * @return the list
	 *
	 * @throws SQLException
	 */
	<O> List<O> executeQuery(List<SqlParameter> sqlParameters, final Class<O> dataType, final Integer limit) throws SQLException;

	/**
	 * Executes a sql query returning the number of modified rows.
	 *
	 * @param sqlParameters input params
	 * @param dataType the return dataType of the list
	 * @param limit the return limit (null if no limit)
	 * @return either the row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing
	 * @throws SQLException
	 */
	int executeUpdate(List<SqlParameter> sqlParameters) throws SQLException;

	/**
	 * Executes the batch .
	 * @throws SQLException Si erreur
	 * @return the SUM of  row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing
	 * @throws SQLException
	 */
	int executeBatch(List<List<SqlParameter>> parameters) throws SQLException;

	/**
	 * Returns the generated keys when an insert is executed.
	 * @param columnName the column name
	 * @param domain the domain
	 * @throws SQLException
	 */
	<O> O getGeneratedKey(final String columnName, final Class<O> dataType) throws SQLException;

	/**
	 * closes without exceoption.
	 */
	@Override
	void close();
}
