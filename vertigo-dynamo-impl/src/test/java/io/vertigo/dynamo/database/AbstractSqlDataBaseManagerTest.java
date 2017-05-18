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
package io.vertigo.dynamo.database;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.connection.SqlConnectionProvider;
import io.vertigo.dynamo.database.data.Movie;
import io.vertigo.dynamo.database.data.MovieInfo;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;

/**
 *
 * @author pchretien
 */
public abstract class AbstractSqlDataBaseManagerTest extends AbstractTestCaseJU4 {
	private static final String TITLE_MOVIE_1 = "citizen kane";
	private static final String TITLE_MOVIE_2 = "vertigo";
	private static final String TITLE_MOVIE_3 = "gone girl";
	private static final String TITLE_MOVIE_4 = "Jurassic Park";
	@Inject
	private SqlDataBaseManager dataBaseManager;

	protected SqlConnection obtainMainConnection() {
		return dataBaseManager
				.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME)
				.obtainConnection();
	}

	@Override
	protected void doSetUp() throws Exception {
		//A chaque test on recrée la table famille
		final SqlConnection connection = obtainMainConnection();
		try {
			execCallableStatement(connection, "create table movie(id BIGINT , title varchar(255));");
		} finally {
			connection.release();
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		//A chaque fin de test on arrête la base.
		final SqlConnection connection = obtainMainConnection();
		try {
			execCallableStatement(connection, "shutdown;");
		} finally {
			connection.release();
		}

	}

	@Test
	public void testConnection() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			Assert.assertNotNull(connection);
			connection.commit();
		} finally {
			connection.release();
		}
	}

	protected void execCallableStatement(final SqlConnection connection, final String sql) throws SQLException {
		try (final SqlPreparedStatement preparedStatement = dataBaseManager.createPreparedStatement(connection, sql, false)) {
			preparedStatement.executeUpdate();
		}
	}

	private void insert(final SqlConnection connection, final long key, final String libelle) throws SQLException {
		final String sql = "insert into movie values (?, ?)";
		try (final SqlPreparedStatement preparedStatement = dataBaseManager.createPreparedStatement(connection, sql, false)) {
			preparedStatement.setValue(0, Long.class, key);
			preparedStatement.setValue(1, String.class, libelle);
			//-----
			preparedStatement.executeUpdate();
		}
	}

	public void createDatas() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			execCallableStatement(connection, "insert into movie values (1, 'citizen kane')");
			//-----
			execCallableStatement(connection, "insert into movie values (2, 'vertigo')");
			//-----
			//On passe par une requête bindée
			insert(connection, 3, TITLE_MOVIE_3);
			connection.commit();
		} finally {
			connection.release();
		}
	}

	@Test
	public void testCallableStatement() throws Exception {
		createDatas();
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectEntities() throws Exception {
		createDatas();
		//----
		final List<Movie> movies = executeQuery(Movie.class, "select * from movie", null);

		Assert.assertEquals(3, movies.size());

		for (final Movie movie : movies) {
			final long id = movie.getId();
			final String title = movie.getTitle();
			checkTitle(id, title);
		}
	}

	@Test
	public void testSelectSimpleObjects() throws Exception {
		createDatas();
		//----
		final List<MovieInfo> movieInfos = executeQuery(MovieInfo.class, "select title from movie", null);
		Assert.assertEquals(3, movieInfos.size());
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

	@Test
	public void testSelectObject() throws Exception {
		createDatas();
		//----
		final List<Movie> movies = executeQuery(Movie.class, "select * from movie where id=1", null);
		Assert.assertEquals(1, movies.size());
		final Movie movie = movies.get(0);
		Assert.assertEquals("citizen kane", movie.getTitle());
	}

	private <O> List<O> executeQuery(final Class<O> dataType, final String sql, final Integer limit) throws SQLException, Exception {
		return executeQuery(dataType, sql, dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME), limit);
	}

	private <O> List<O> executeQuery(
			final Class<O> dataType,
			final String sql,
			final SqlConnectionProvider sqlConnectionProvider,
			final Integer limit) throws SQLException, Exception {
		final SqlConnection connection = sqlConnectionProvider.obtainConnection();
		try {
			try (final SqlPreparedStatement preparedStatement = dataBaseManager.createPreparedStatement(connection, sql, false)) {
				return preparedStatement.executeQuery(dataType, limit);
			}
		} finally {
			connection.release();
		}
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testSelectPrimitive() throws Exception {
		createDatas();
		//----
		final List<Integer> result = executeQuery(Integer.class, "select count(*) from movie", 1);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(3, result.get(0).intValue());
	}

	@Test
	public void testSelectPrimitive2() throws Exception {
		createDatas();
		//----
		final List<String> result = executeQuery(String.class, "select title from movie where id=1", 1);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(TITLE_MOVIE_1, result.get(0));
	}

	@Test
	public void testSelectPrimitive3() throws Exception {
		//On crée les données
		createDatas();
		//----
		final List<String> result = executeQuery(String.class, "select title from movie", null);
		Assert.assertEquals(3, result.size());
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testTwoDataSource() throws Exception {
		createDatas();

		setupSecondary();
		try {
			//on crée des données dans 'secondary'
			final SqlConnection connection = dataBaseManager.getConnectionProvider("secondary").obtainConnection();
			try {
				execCallableStatement(connection, "insert into movie values (1, 'Star wars')");
				execCallableStatement(connection, "insert into movie values (2, 'Will Hunting')");
				execCallableStatement(connection, "insert into movie values (3, 'Usual Suspects')");
				//-----
				//On passe par une requête bindée
				insert(connection, 4, TITLE_MOVIE_4);
				connection.commit();
			} finally {
				connection.release();
			}
			//----
			final List<Integer> result2 = executeQuery(Integer.class, "select count(*) from movie", dataBaseManager.getConnectionProvider("secondary"), 1);
			Assert.assertEquals(1, result2.size());
			Assert.assertEquals(4, result2.get(0).intValue());
			final List<Movie> resultMovie1 = executeQuery(Movie.class, "select * from movie where id=1", dataBaseManager.getConnectionProvider("secondary"), 1);
			Assert.assertEquals(1, resultMovie1.size());
			Assert.assertEquals("Star wars", resultMovie1.get(0).getTitle());

			final List<Integer> result1 = executeQuery(Integer.class, "select count(*) from movie", dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME), 1);
			Assert.assertEquals(1, result1.size());
			Assert.assertEquals(3, result1.get(0).intValue());
		} finally {
			shutdownSecondaryDown();
		}
	}

	private void setupSecondary() throws Exception {
		//A chaque test on recrée la table famille
		final SqlConnection connection = dataBaseManager.getConnectionProvider("secondary").obtainConnection();
		try {
			execCallableStatement(connection, "create table movie(id BIGINT , title varchar(255));");
		} finally {
			connection.release();
		}
	}

	private void shutdownSecondaryDown() throws Exception {
		//A chaque fin de test on arrête la base.
		final SqlConnection connection = dataBaseManager.getConnectionProvider("secondary").obtainConnection();
		try {
			execCallableStatement(connection, "shutdown;");
		} finally {
			connection.release();
		}
	}
}
