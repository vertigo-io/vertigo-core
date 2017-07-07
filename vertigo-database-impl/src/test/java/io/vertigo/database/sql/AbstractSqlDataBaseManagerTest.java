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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.OptionalInt;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.data.Movie;
import io.vertigo.database.sql.data.MovieInfo;
import io.vertigo.database.sql.data.Movies;
import io.vertigo.database.sql.statement.SqlParameter;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;
import io.vertigo.lang.DataStream;

/**
 *
 * @author pchretien
 */
public abstract class AbstractSqlDataBaseManagerTest extends AbstractTestCaseJU4 {
	private static final String DROP_TABLE_MOVIE = "DROP TABLE movie";
	private static final String DROP_SEQUENCE_MOVIE = "DROP SEQUENCE seq_movie";

	private static final String INSERT_INTO_MOVIE_VALUES = "insert into movie values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String CREATE_TABLE_MOVIE = "create table movie ("
			+ "id bigint , "
			+ "title varchar(255) , "
			+ "fps double precision ,"
			+ "income decimal(6,3) , "
			+ "color boolean , "
			+ "release_date timestamp , "
			+ "release_local_date date , "
			+ "release_zoned_date_time datetime , "
			+ "icon blob );";
	@Inject
	protected SqlDataBaseManager dataBaseManager;

	protected SqlConnection obtainMainConnection() {
		return dataBaseManager
				.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME)
				.obtainConnection();
	}

	@Override
	protected final void doSetUp() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			execpreparedStatement(connection, createTableMovie());
			execpreparedStatement(connection, createSequenceMovie());
			if (commitRequiredOnSchemaModification()) {
				connection.commit();
			}
		} finally {
			connection.release();
		}
	}

	protected abstract String createTableMovie();

	protected abstract String createSequenceMovie();

	protected abstract boolean commitRequiredOnSchemaModification();

	@Override
	protected final void doTearDown() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			// we use a shared database so we need to drop the table
			execpreparedStatement(connection, DROP_SEQUENCE_MOVIE);
			execpreparedStatement(connection, DROP_TABLE_MOVIE);
			if (commitRequiredOnSchemaModification()) {
				connection.commit();
			}
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

	protected void execpreparedStatement(final SqlConnection connection, final String sql) throws SQLException {
		dataBaseManager
				.createPreparedStatement(connection)
				.executeUpdate(sql, Collections.emptyList());
	}

	private void insert(
			final SqlConnection connection,
			final long id,
			final String title,
			final Date releaseDate,
			final LocalDate releaseLocalDate,
			final ZonedDateTime releaseZonedDateTime) throws SQLException {

		final String sql = INSERT_INTO_MOVIE_VALUES;
		final List<SqlParameter> sqlParameters = Arrays.asList(
				SqlParameter.of(Long.class, id),
				SqlParameter.of(String.class, title),
				SqlParameter.of(Double.class, null),
				SqlParameter.of(BigDecimal.class, null),
				SqlParameter.of(Boolean.class, null),
				SqlParameter.of(Date.class, releaseDate),
				SqlParameter.of(LocalDate.class, releaseLocalDate),
				SqlParameter.of(ZonedDateTime.class, releaseZonedDateTime),
				SqlParameter.of(DataStream.class, Movies.buildIcon()));
		//-----
		dataBaseManager.createPreparedStatement(connection)
				.executeUpdate(sql, sqlParameters);
	}

	private void createDatas() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			insert(connection, 1,
					Movies.TITLE_MOVIE_1,
					new Date(1941 - 1900, 5 - 1, 1, 16, 30),
					LocalDate.of(1941, 5, 1),
					ZonedDateTime.of(LocalDate.of(1941, 5, 1), LocalTime.of(16, 30), ZoneId.of("UTC")));
			//-----
			insert(connection, 2,
					Movies.TITLE_MOVIE_2,
					new Date(1958 - 1900, 5 - 1, 9, 16, 30),
					LocalDate.of(1958, 5, 9),
					ZonedDateTime.of(LocalDate.of(1958, 5, 9), LocalTime.of(16, 30), ZoneId.of("UTC")));
			//-----
			//On passe par une requête bindée
			insert(connection, 3, Movies.TITLE_MOVIE_3, null, null, null);
			connection.commit();
		} finally {
			connection.release();
		}
	}

	@Test
	public void testPreparedStatement() throws Exception {
		createDatas();
	}

	@Test
	public void testSelectEntities() throws Exception {
		createDatas();
		//----
		final List<Movie> movies = executeQuery(Movie.class, "select * from movie", null);

		Assert.assertEquals(3, movies.size());
		movies.forEach(Movies::checkMovie);
	}

	@Test
	public void testSelectSimpleObjects() throws Exception {
		createDatas();
		//----
		final List<MovieInfo> movieInfos = executeQuery(MovieInfo.class, "select title from movie", null);
		Assert.assertEquals(3, movieInfos.size());
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

	protected final <O> List<O> executeQuery(final Class<O> dataType, final String sql, final Integer limit) throws SQLException, Exception {
		final SqlConnectionProvider sqlConnectionProvider = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME);
		return executeQuery(dataType, sql, sqlConnectionProvider, limit);
	}

	private <O> List<O> executeQuery(
			final Class<O> dataType,
			final String sql,
			final SqlConnectionProvider sqlConnectionProvider,
			final Integer limit) throws SQLException, Exception {
		final SqlConnection connection = sqlConnectionProvider.obtainConnection();
		try {
			return dataBaseManager
					.createPreparedStatement(connection)
					.executeQuery(sql, Collections.emptyList(), dataType, limit);
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
		Assert.assertEquals(Movies.TITLE_MOVIE_1, result.get(0));
	}

	@Test
	public void testSelectPrimitive3() throws Exception {
		//On crée les données
		createDatas();
		//----
		final List<String> result = executeQuery(String.class, "select title from movie", null);
		Assert.assertEquals(3, result.size());
	}

	@Test
	public void testBatchInserts() throws Exception {
		final SqlConnectionProvider sqlConnectionProvider = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME);
		final String sql = INSERT_INTO_MOVIE_VALUES;

		final List<Movie> movies = Movies.bondMovies();

		//--prepare data
		final List<List<SqlParameter>> batch = new ArrayList<>();
		for (final Movie movie : movies) {
			final List<SqlParameter> sqlParameters = Arrays.asList(
					SqlParameter.of(Long.class, movie.getId()),
					SqlParameter.of(String.class, movie.getTitle()),
					SqlParameter.of(Double.class, movie.getFps()),
					SqlParameter.of(BigDecimal.class, movie.getIncome()),
					SqlParameter.of(Boolean.class, movie.getColor()),
					SqlParameter.of(Date.class, movie.getReleaseDate()),
					SqlParameter.of(LocalDate.class, movie.getReleaseLocalDate()),
					SqlParameter.of(ZonedDateTime.class, movie.getReleaseZonedDateTime()),
					SqlParameter.of(DataStream.class, movie.getIcon()));
			batch.add(sqlParameters);
		}

		final SqlConnection connection = sqlConnectionProvider.obtainConnection();
		final OptionalInt result;
		try {
			result = dataBaseManager
					.createPreparedStatement(connection)
					.executeBatch(sql, batch);
			connection.commit();
		} finally {
			connection.release();
		}
		//---
		if (result.isPresent()) {
			Assert.assertEquals(movies.size(), result.getAsInt());
		}
		final List<Integer> countMovie = executeQuery(Integer.class, "select count(*) from movie", 1);
		Assert.assertEquals(movies.size(), countMovie.get(0).intValue());
	}

	//On teste un preparestatement mappé sur un type statique (Class famille)
	@Test
	public void testTwoDataSource() throws Exception {
		createDatas();
		setupSecondary();

		//on crée des données dans 'secondary'
		final SqlConnection connection = dataBaseManager.getConnectionProvider("secondary")
				.obtainConnection();
		try {
			execpreparedStatement(connection, "insert into movie values (1, 'Star wars', null, null, null, null, null, null, null)");
			execpreparedStatement(connection, "insert into movie values (2, 'Will Hunting', null, null, null, null, null, null, null)");
			execpreparedStatement(connection, "insert into movie values (3, 'Usual Suspects', null, null, null, null, null, null, null)");
			//-----
			//On passe par une requête bindée
			insert(connection, 4, Movies.TITLE_MOVIE_4, null, null, null);
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
	}

	private void setupSecondary() throws Exception {
		//A chaque test on recrée la table famille
		final SqlConnection connection = dataBaseManager.getConnectionProvider("secondary")
				.obtainConnection();
		try {
			execpreparedStatement(connection, CREATE_TABLE_MOVIE);
		} finally {
			connection.release();
		}
	}

	protected abstract GenerationMode getExpectedGenerationMode();

	@Test
	public final void testInsert() throws Exception {
		final String insertWithgeneratedKey = obtainMainConnection().getDataBase().getSqlDialect()
				.createInsertQuery(
						"ID",
						Arrays.asList("TITLE"),
						"seq_",
						"movie");

		final GenerationMode generationMode = obtainMainConnection().getDataBase().getSqlDialect().getGenerationMode();
		//We check that we have the right expected mode
		Assert.assertEquals(getExpectedGenerationMode(), generationMode);
		//---
		final SqlConnection connection = obtainMainConnection();
		long generatedKey;
		try {
			final String sql = dataBaseManager.parseQuery(insertWithgeneratedKey).getVal1();
			generatedKey = dataBaseManager.createPreparedStatement(connection)
					.executeUpdateWithGeneratedKey(
							sql,
							Arrays.asList(SqlParameter.of(String.class, "frankenstein")),
							generationMode,
							"ID",
							Long.class)
					.getVal2();
			connection.commit();
		} finally {
			connection.release();
		}
		final List<Integer> result = executeQuery(Integer.class, "select count(*) from movie", null);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(1, result.get(0).intValue());

		final List<Integer> keys = executeQuery(Integer.class, "select id from movie", null);
		Assert.assertEquals(1, keys.size());
		Assert.assertEquals(generatedKey, keys.get(0).intValue());
	}
}
