/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.database.impl.sql.vendor.sqlserver.SqlServerDataBase;
import io.vertigo.database.sql.vendor.SqlDialect;

/**
 *
 * @author mlaroche
 */
public abstract class AbstractSqlDialectTest {

	@Test
	public void testInsertQuery() {

		final SqlDialect sqlDialect = new SqlServerDataBase().getSqlDialect();
		final String insertQuery = sqlDialect.createInsertQuery("ID", Collections.singletonList("TITLE"), "SEQ_", "MOVIE");
		Assertions.assertEquals(getExpectedInsertQuery(), insertQuery);
	}

	@Test
	public void testSelectForUpdateWildcardQuery() {
		final String selectForUpdateQuery = getDialect().createSelectForUpdateQuery("MOVIE", "*", "ID");
		Assertions.assertEquals(getExpectedSelectForUpdateWildCardQuery(), selectForUpdateQuery);
	}

	@Test
	public void testSelectForUpdateFieldsQuery() {
		final String selectForUpdateQuery = getDialect().createSelectForUpdateQuery("MOVIE", "ID, TITLE", "ID");
		Assertions.assertEquals(getExpectedSelectForUpdateFieldsQuery(), selectForUpdateQuery);
	}

	@Test
	public void testAppendMaxRows() {
		final StringBuilder stringBuilder = new StringBuilder("select * from MOVIE");
		getDialect().appendMaxRows(stringBuilder, 100);
		final String query = stringBuilder.toString();
		Assertions.assertEquals(getExpectedAppendMaxRowsQuery(), query);
	}

	public abstract SqlDialect getDialect();

	public abstract String getExpectedInsertQuery();

	public abstract String getExpectedSelectForUpdateWildCardQuery();

	public abstract String getExpectedSelectForUpdateFieldsQuery();

	public abstract Optional<String> getExpectedCreatePrimaryKeyQuery();

	public abstract String getExpectedAppendMaxRowsQuery();

}
