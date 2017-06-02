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
package io.vertigo.database;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.vertigo.database.sql.parser.SqlParserTest;
import io.vertigo.database.sql.vendor.h2.H2DataBaseManagerTest;
import io.vertigo.database.sql.vendor.h2.H2SqlDialectTest;
import io.vertigo.database.sql.vendor.hsql.HSqlDataBaseManagerTest;
import io.vertigo.database.sql.vendor.hsql.HSqlDialectTest;
import io.vertigo.database.sql.vendor.oracle.OracleDataBaseManagerTest;
import io.vertigo.database.sql.vendor.oracle.OracleDialectTest;
import io.vertigo.database.sql.vendor.postgresql.PostgreSqlDialectTest;
import io.vertigo.database.sql.vendor.sqlserver.SqlServerDialectTest;

/**
 * This suite contains all the tests for 'dynamo' module.
 *
 * @author pchretien
 */
@RunWith(Suite.class)
@SuiteClasses({
		//--sql
		H2SqlDialectTest.class,
		HSqlDialectTest.class,
		OracleDialectTest.class,
		PostgreSqlDialectTest.class,
		SqlServerDialectTest.class,
		//--
		H2DataBaseManagerTest.class,
		HSqlDataBaseManagerTest.class,
		OracleDataBaseManagerTest.class,
		//--
		SqlParserTest.class
})

public final class DatabaseTestSuite {
	//
}
