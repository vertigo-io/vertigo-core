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

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.connection.SqlConnectionProvider;
import io.vertigo.database.sql.data.Movie;
import io.vertigo.database.sql.data.MovieInfo;
import io.vertigo.database.sql.statement.SqlParameter;
import io.vertigo.database.sql.statement.SqlPreparedStatement;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;
import io.vertigo.util.ListBuilder;

/**
 *
 * @author pchretien
 */
public abstract class AbstractSqlDataBaseManagerTest extends AbstractTestCaseJU4 {
	private static final String INSERT_INTO_MOVIE_VALUES = "insert into movie values (?, ?, ?, ?)";
	private static final String CREATE_TABLE_MOVIE = "create table movie(id BIGINT , title varchar(255), release_Date date, release_Local_Date date);";
	private static final String TITLE_MOVIE_1 = "citizen kane"; //1 May 1941
	private static final String TITLE_MOVIE_2 = "vertigo"; //9 May 1958
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
			execpreparedStatement(connection, CREATE_TABLE_MOVIE);
		} finally {
			connection.release();
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		//A chaque fin de test on arrête la base.
		final SqlConnection connection = obtainMainConnection();
		try {
			execpreparedStatement(connection, "shutdown;");
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
		try (final SqlPreparedStatement preparedStatement = dataBaseManager.createPreparedStatement(connection, sql, GenerationMode.NONE)) {
			preparedStatement.executeUpdate(Collections.emptyList());
		}
	}

	private void insert(final SqlConnection connection, final long key, final String libelle, final Date releaseDate, final LocalDate releaselocalDate) throws SQLException {
		final String sql = INSERT_INTO_MOVIE_VALUES;
		try (final SqlPreparedStatement preparedStatement = dataBaseManager.createPreparedStatement(connection, sql, GenerationMode.NONE)) {
			final List<SqlParameter> sqlParameters = new ListBuilder<SqlParameter>()
					.add(new SqlParameter(Long.class, key))
					.add(new SqlParameter(String.class, libelle))
					.add(new SqlParameter(Date.class, releaseDate))
					.add(new SqlParameter(LocalDate.class, releaselocalDate))
					.build();
			//-----
			preparedStatement.executeUpdate(sqlParameters);
		}
	}

	private void createDatas() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			execpreparedStatement(connection, "insert into movie values (1, 'citizen kane', '1941-05-01', '1941-05-01')");
			//-----
			execpreparedStatement(connection, "insert into movie values (2, 'vertigo', '1958-05-09', '1958-05-09')");
			//-----
			//On passe par une requête bindée
			insert(connection, 3, TITLE_MOVIE_3, null, null);
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
		movies.forEach(this::checkMovie);
	}

	@Test
	public void testSelectSimpleObjects() throws Exception {
		createDatas();
		//----
		final List<MovieInfo> movieInfos = executeQuery(MovieInfo.class, "select title from movie", null);
		Assert.assertEquals(3, movieInfos.size());
	}

	private void checkMovie(final Movie movie) {
		switch (movie.getId().intValue()) {
			case 1:
				Assert.assertEquals(TITLE_MOVIE_1, movie.getTitle());
				Assert.assertEquals("01-05-1941", new SimpleDateFormat("dd-MM-YYYY").format(movie.getReleaseDate()));
				Assert.assertEquals(1, movie.getReleaseLocalDate().getDayOfMonth());
				Assert.assertEquals(5, movie.getReleaseLocalDate().getMonthValue());
				Assert.assertEquals(1941, movie.getReleaseLocalDate().getYear());

				break;
			case 2:
				Assert.assertEquals(TITLE_MOVIE_2, movie.getTitle());
				Assert.assertEquals("09-05-1958", new SimpleDateFormat("dd-MM-YYYY").format(movie.getReleaseDate()));
				Assert.assertEquals(9, movie.getReleaseLocalDate().getDayOfMonth());
				Assert.assertEquals(5, movie.getReleaseLocalDate().getMonthValue());
				Assert.assertEquals(1958, movie.getReleaseLocalDate().getYear());
				break;
			case 3:
				Assert.assertEquals(TITLE_MOVIE_3, movie.getTitle());
				Assert.assertEquals(null, movie.getReleaseDate());
				Assert.assertEquals(null, movie.getReleaseLocalDate());
				break;
			default:
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
			try (final SqlPreparedStatement preparedStatement = dataBaseManager.createPreparedStatement(connection, sql, GenerationMode.NONE)) {
				return preparedStatement.executeQuery(Collections.emptyList(), dataType, limit);
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

	@Test
	public void testBatchInserts() throws Exception {
		final List<Movie> bondList = new ListBuilder<Movie>()
				.add(createMovie(1, "Doctor No", 1962, 10, 5))//5 October 1962
				.add(createMovie(2, "From Russia with Love", 1963, 10, 10)) //10 October 1963
				.add(createMovie(3, "Goldfinger", 1964, 9, 17)) //17 September 1964
				.add(createMovie(4, "Thunderball", 1965, 12, 9)) //9 December 1965
				.add(createMovie(5, "You only live twice", 1967, 6, 12)) //12 June 1967
				.add(createMovie(6, "On her majesty's secret service", 1969, 12, 13)) //13 December 1969
				.add(createMovie(7, "Diamonds are forever", 1971, 12, 14)) //14 December 1971
				.add(createMovie(8, "Live and let die", 1973, 6, 27)) //27 June 1973
				.add(createMovie(9, "The man with the golden gun", 1974, 12, 14)) //14 December 1974
				.add(createMovie(10, "The spy who loved me", 1977, 7, 7)) //7 July 1977
				.add(createMovie(11, "Moonraker"))
				.add(createMovie(12, "For your eyes only"))
				.add(createMovie(13, "Octopussy"))
				.add(createMovie(14, "A view to a kill"))
				.add(createMovie(15, "The living daylights"))
				.build();

		final SqlConnectionProvider sqlConnectionProvider = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME);
		final String sql = INSERT_INTO_MOVIE_VALUES;
		final int result;

		final SqlConnection connection = sqlConnectionProvider.obtainConnection();
		try {
			try (final SqlPreparedStatement preparedStatement = dataBaseManager.createPreparedStatement(connection, sql, GenerationMode.NONE)) {
				for (int i = 0; i < bondList.size(); i++) {
					final Movie movie = bondList.get(i);
					preparedStatement.addBatch(Arrays.asList(
							new SqlParameter(Long.class, movie.getId()),
							new SqlParameter(String.class, movie.getTitle()),
							new SqlParameter(Date.class, movie.getReleaseDate()),
							new SqlParameter(LocalDate.class, movie.getReleaseLocalDate())));
				}
				result = preparedStatement.executeBatch();
			}
			connection.commit();
		} finally {
			connection.release();
		}
		//---
		Assert.assertEquals(bondList.size(), result);
		final List<Integer> countMovie = executeQuery(Integer.class, "select count(*) from movie", 1);
		Assert.assertEquals(bondList.size(), countMovie.get(0).intValue());
	}

	private Movie createMovie(final long id, final String title, final int year, final int month, final int dayOfMonth) {
		final Calendar calendar = new GregorianCalendar();
		calendar.set(year, month, dayOfMonth);
		return createMovie(id, title, calendar.getTime(), LocalDate.of(year, month, dayOfMonth));
	}

	private Movie createMovie(final long id, final String title) {
		return createMovie(id, title, null, null);
	}

	private Movie createMovie(final long id, final String title, final Date date, final LocalDate localDate) {
		final Movie movie = new Movie();
		movie.setId(id);
		movie.setTitle(title);
		movie.setReleaseDate(date);
		movie.setReleaseLocalDate(localDate);
		return movie;

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
				execpreparedStatement(connection, "insert into movie values (1, 'Star wars', null, null)");
				execpreparedStatement(connection, "insert into movie values (2, 'Will Hunting', null, null)");
				execpreparedStatement(connection, "insert into movie values (3, 'Usual Suspects', null, null)");
				//-----
				//On passe par une requête bindée
				insert(connection, 4, TITLE_MOVIE_4, null, null);
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
			execpreparedStatement(connection, CREATE_TABLE_MOVIE);
		} finally {
			connection.release();
		}
	}

	private void shutdownSecondaryDown() throws Exception {
		//A chaque fin de test on arrête la base.
		final SqlConnection connection = dataBaseManager.getConnectionProvider("secondary").obtainConnection();
		try {
			execpreparedStatement(connection, "shutdown;");
		} finally {
			connection.release();
		}
	}
}
