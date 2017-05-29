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
package io.vertigo.database.sql;

import java.util.List;

import io.vertigo.core.component.Manager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.parser.SqlNamedParam;
import io.vertigo.database.sql.statement.SqlPreparedStatement;
import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.lang.Tuples.Tuple2;

/**
 * Manages connections to database.
 * @author pchretien
 */
public interface SqlDataBaseManager extends Manager {
	/** Main connectionProvider's name. */
	String MAIN_CONNECTION_PROVIDER_NAME = "main";

	/**
	 * @param name ConnectionProvider name
	 * @return SecondaryConnectionProvider
	 */
	SqlConnectionProvider getConnectionProvider(String name);

	/**
	 * @param connection Connection
	 * @param sql Requête SQL
	 * @param generatedKeys Si on récupère les clés générées par la base de données.
	 * @return Statement
	 */
	SqlPreparedStatement createPreparedStatement(
			final SqlConnection connection,
			final String sql,
			final SqlDialect.GenerationMode generationMode,
			final String... generatedColumns);

	/**
	 * Analyses a query and extracts all the namedParams.
	 * @param query the original bound query
	 * @return the sql query and named params
	 */
	Tuple2<String, List<SqlNamedParam>> parseQuery(final String query);
}
