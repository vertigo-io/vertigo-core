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
package io.vertigo.dynamo.database.vendor;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.database.data.domain.Movie;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.database.vendor.sqlserver.SqlServerDataBase;

/**
 *
 * @author mlaroche
 */
public abstract class AbstractSqlDialectTest extends AbstractTestCaseJU4 {

	@Test
	public void testInsertQuery() {

		final SqlDialect sqlDialect = new SqlServerDataBase().getSqlDialect();
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Movie.class);

		final String insertQuery = sqlDialect.createInsertQuery(dtDefinition, "SEQ_", "MOVIE");
		System.out.println(insertQuery);
		System.out.println(getExpectedInsertQuery());
		Assert.assertEquals(getExpectedInsertQuery(), insertQuery);
	}

	@Test
	public void testSelectForUpdateWildcardQuery() {
		final String selectForUpdateQuery = getDialect().createSelectForUpdateQuery("MOVIE", "*", "ID");
		System.out.println(selectForUpdateQuery);
		System.out.println(getExpectedSelectForUpdateWildCardQuery());
		Assert.assertEquals(getExpectedSelectForUpdateWildCardQuery(), selectForUpdateQuery);
	}

	@Test
	public void testSelectForUpdateFieldsQuery() {
		final String selectForUpdateQuery = getDialect().createSelectForUpdateQuery("MOVIE", "ID, TITLE", "ID");
		System.out.println(selectForUpdateQuery);
		System.out.println(getExpectedSelectForUpdateFieldsQuery());
		Assert.assertEquals(getExpectedSelectForUpdateFieldsQuery(), selectForUpdateQuery);
	}

	@Test
	public void testCreatePrimaryKeyQuery() {
		final Optional<String> primaryKeyQuery = getDialect().createPrimaryKeyQuery("MOVIE", "SEQ_");
		System.out.println(primaryKeyQuery);
		System.out.println(getExpectedCreatePrimaryKeyQuery());

		if (primaryKeyQuery.isPresent()) {
			Assert.assertTrue(getExpectedCreatePrimaryKeyQuery().isPresent());
			Assert.assertEquals(getExpectedCreatePrimaryKeyQuery().get(), primaryKeyQuery.get());
		} else {
			Assert.assertFalse(getExpectedCreatePrimaryKeyQuery().isPresent());
		}
	}

	@Test
	public void testAppendMaxRows() {
		final StringBuilder stringBuilder = new StringBuilder("select * from MOVIE");
		getDialect().appendMaxRows(stringBuilder, 100);
		final String query = stringBuilder.toString();

		System.out.println(query);
		System.out.println(getExpectedAppendMaxRowsQuery());

		Assert.assertEquals(getExpectedAppendMaxRowsQuery(), query);
	}

	abstract SqlDialect getDialect();

	abstract String getExpectedInsertQuery();

	abstract String getExpectedSelectForUpdateWildCardQuery();

	abstract String getExpectedSelectForUpdateFieldsQuery();

	abstract Optional<String> getExpectedCreatePrimaryKeyQuery();

	abstract String getExpectedAppendMaxRowsQuery();

}
