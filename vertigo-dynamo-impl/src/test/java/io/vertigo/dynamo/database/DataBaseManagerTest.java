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

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.data.Movie;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.database.statement.SqlQueryResult;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author pchretien
 */
public class DataBaseManagerTest extends AbstractTestCaseJU4 {
	private static final String TITLE_MOVIE_1 = "citizen kane";
	private static final String TITLE_MOVIE_2 = "vertigo";
	private static final String TITLE_MOVIE_3 = "gone girl";
	@Inject
	private SqlDataBaseManager dataBaseManager;

	@Override
	protected void doSetUp() throws Exception {
		//A chaque test on recrée la table famille
		final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		execCallableStatement(connection, "create table movie(id BIGINT , title varchar(255));");
	}

	@Override
	protected void doTearDown() throws Exception {
		//A chaque fin de test on arrête la base.
		final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		execCallableStatement(connection, "shutdown;");
	}

	@Test
	public void testConnection() throws Exception {
		final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		Assert.assertNotNull(connection);
		connection.commit();
	}

	private void execCallableStatement(final SqlConnection connection, final String sql) throws SQLException {
		final SqlCallableStatement callableStatement = dataBaseManager.createCallableStatement(connection, sql);
		callableStatement.init();
		callableStatement.executeUpdate();
	}

	private void insert(final SqlConnection connection, final long key, final String libelle) throws SQLException {
		final String sql = "insert into movie values (?, ?)";
		try (final SqlCallableStatement callableStatement = dataBaseManager.createCallableStatement(connection, sql)) {
			callableStatement.registerParameter(0, DataType.Long, true);
			callableStatement.registerParameter(1, DataType.String, true);
			//-----
			callableStatement.init();
			//-----
			callableStatement.setValue(0, key);
			callableStatement.setValue(1, libelle);
			//-----
			callableStatement.executeUpdate();
		}
	}

	public void createDatas() throws Exception {
		final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		try {
			execCallableStatement(connection, "insert into movie values (1, 'citizen kane')");
			//-----
			execCallableStatement(connection, "insert into movie values (2, 'vertigo')");
			//-----
			//On passe par une requête bindée
			insert(connection, 3, TITLE_MOVIE_3);
		} finally {
			connection.commit();
		}
	}

	@Test
	public void testCallableStatement() throws Exception {
		createDatas();
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectList() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = Home.getDefinitionSpace().resolve("DO_DT_MOVIE_DTC", Domain.class);
		final SqlQueryResult result = executeQuery(domain, "select * from movie");

		Assert.assertEquals(3, result.getSQLRowCount());

		final DtList<Movie> movies = (DtList<Movie>) result.getValue();
		Assert.assertEquals(3, movies.size());

		for (final Movie movie : movies) {
			final long id = movie.getId();
			final String title = movie.getTitle();
			checkTitle(id, title);
		}
	}

	private static void checkTitle(final long id, final String title) {
		if (id == 1) {
			Assert.assertEquals(TITLE_MOVIE_1, title);
		} else if (id == 2) {
			Assert.assertEquals(TITLE_MOVIE_2, title);
		} else if (id == 3) {
			Assert.assertEquals(TITLE_MOVIE_3, title);
		} else {
			Assert.fail();
		}
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectObject() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = Home.getDefinitionSpace().resolve("DO_DT_MOVIE_DTO", Domain.class);
		final SqlQueryResult result = executeQuery(domain, "select * from movie where id=1");
		Assert.assertEquals(1, result.getSQLRowCount());
		final Movie movie = (Movie) result.getValue();
		Assert.assertEquals("citizen kane", movie.getTitle());
	}

	private SqlQueryResult executeQuery(final Domain domain, final String sql) throws SQLException, Exception {
		final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		try (final SqlPreparedStatement preparedStatement = dataBaseManager.createPreparedStatement(connection, sql, false)) {
			preparedStatement.init();
			return preparedStatement.executeQuery(domain);
		} finally {
			connection.commit();
		}
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectPrimitive() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = new Domain("DO_INTEGER", DataType.Integer);
		final SqlQueryResult result = executeQuery(domain, "select count(*) from movie");
		Assert.assertEquals(1, result.getSQLRowCount());
		Assert.assertEquals(3, result.getValue());
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectPrimitive2() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = new Domain("DO_LIB", DataType.String);
		final SqlQueryResult result = executeQuery(domain, "select title from movie where id=1");
		Assert.assertEquals(1, result.getSQLRowCount());
		Assert.assertEquals(TITLE_MOVIE_1, result.getValue());
	}

	//On teste un preparestatement mappé sur un type dynamique DTList.
	@Test
	public void testDynSelect() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = new Domain("DO_TEST", DataType.DtList);
		final SqlQueryResult result = executeQuery(domain, "select * from movie");

		Assert.assertEquals(3, result.getSQLRowCount());
		final DtList<DtObject> dynMovies = (DtList<DtObject>) result.getValue();
		Assert.assertEquals(3, dynMovies.size());

		for (final DtObject dynMovie : dynMovies) {
			final long id = (Long) getValue(dynMovie, "ID");
			final String title = (String) getValue(dynMovie, "TITLE");
			checkTitle(id, title);
		}
	}

	private static Object getValue(final DtObject dto, final String fieldName) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(fieldName);
		return dtField.getDataAccessor().getValue(dto);
	}

}
