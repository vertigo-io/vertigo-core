/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.connection.SqlConnectionProvider;
import io.vertigo.dynamo.database.data.domain.Movie;
import io.vertigo.dynamo.database.data.domain.MovieInfo;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.database.statement.SqlQueryResult;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DomainBuilder;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 *
 * @author pchretien
 */
public final class DataBaseManagerTest extends AbstractTestCaseJU4 {
	private static final String TITLE_MOVIE_1 = "citizen kane";
	private static final String TITLE_MOVIE_2 = "vertigo";
	private static final String TITLE_MOVIE_3 = "gone girl";
	private static final String TITLE_MOVIE_4 = "Jurassic Park";
	@Inject
	private SqlDataBaseManager dataBaseManager;
	private DefinitionSpace definitionSpace;

	@Override
	protected void doSetUp() throws Exception {
		//A chaque test on recrée la table famille
		final SqlConnection connection = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
		execCallableStatement(connection, "create table movie(id BIGINT , title varchar(255));");
		definitionSpace = getApp().getDefinitionSpace();
	}

	@Override
	protected void doTearDown() throws Exception {
		//A chaque fin de test on arrête la base.
		final SqlConnection connection = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
		execCallableStatement(connection, "shutdown;");
		definitionSpace = null;
	}

	@Test
	public void testConnection() throws Exception {
		final SqlConnection connection = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
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
		final SqlConnection connection = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
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
	public void testSelectEntities() throws Exception {
		createDatas();
		//----
		final Domain domain = definitionSpace.resolve("DO_DT_MOVIE_DTC", Domain.class);
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

	@Test
	public void testSelectSimpleObjects() throws Exception {
		createDatas();
		//----
		final Domain domain = definitionSpace.resolve("DO_DT_MOVIE_INFO_DTC", Domain.class);
		final SqlQueryResult result = executeQuery(domain, "select title from movie");

		Assert.assertEquals(3, result.getSQLRowCount());

		final DtList<MovieInfo> movieInfos = (DtList<MovieInfo>) result.getValue();
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
		final Domain domain = definitionSpace.resolve("DO_DT_MOVIE_DTO", Domain.class);
		final SqlQueryResult result = executeQuery(domain, "select * from movie where id=1");
		Assert.assertEquals(1, result.getSQLRowCount());
		final Movie movie = (Movie) result.getValue();
		Assert.assertEquals("citizen kane", movie.getTitle());
	}

	private SqlQueryResult executeQuery(final Domain domain, final String sql) throws SQLException, Exception {
		return executeQuery(domain, sql, dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME));
	}

	private SqlQueryResult executeQuery(final Domain domain, final String sql, final SqlConnectionProvider sqlConnectionProvider) throws SQLException, Exception {
		final SqlConnection connection = sqlConnectionProvider.obtainConnection();
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
		createDatas();
		//----
		final Domain domain = new DomainBuilder("DO_INTEGER", DataType.Integer).build();
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
		final Domain domain = new DomainBuilder("DO_LIB", DataType.String).build();
		final SqlQueryResult result = executeQuery(domain, "select title from movie where id=1");
		Assert.assertEquals(1, result.getSQLRowCount());
		Assert.assertEquals(TITLE_MOVIE_1, result.getValue());
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
			} finally {
				connection.commit();
			}
			//----
			final Domain domain = new DomainBuilder("DO_INTEGER", DataType.Integer).build();
			final Domain movieDomain = definitionSpace.resolve("DO_DT_MOVIE_DTO", Domain.class);

			final SqlQueryResult result2 = executeQuery(domain, "select count(*) from movie", dataBaseManager.getConnectionProvider("secondary"));
			Assert.assertEquals(1, result2.getSQLRowCount());
			Assert.assertEquals(4, result2.getValue());
			final SqlQueryResult resultMovie1 = executeQuery(movieDomain, "select * from movie where id=1", dataBaseManager.getConnectionProvider("secondary"));
			Assert.assertEquals(1, resultMovie1.getSQLRowCount());
			Assert.assertEquals("Star wars", ((Movie) resultMovie1.getValue()).getTitle());

			final SqlQueryResult result1 = executeQuery(domain, "select count(*) from movie", dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME));
			Assert.assertEquals(1, result1.getSQLRowCount());
			Assert.assertEquals(3, result1.getValue());
		} finally {
			shutdownSecondaryDown();
		}
	}

	private void setupSecondary() throws Exception {
		//A chaque test on recrée la table famille
		final SqlConnection connection = dataBaseManager.getConnectionProvider("secondary").obtainConnection();
		execCallableStatement(connection, "create table movie(id BIGINT , title varchar(255));");
	}

	private void shutdownSecondaryDown() throws Exception {
		//A chaque fin de test on arrête la base.
		final SqlConnection connection = dataBaseManager.getConnectionProvider("secondary").obtainConnection();
		execCallableStatement(connection, "shutdown;");
	}

	//On teste un preparestatement mappé sur un type dynamique DTList.
	@Test
	public void testDynSelect() throws Exception {
		//On crée les données
		createDatas();
		//----
		final Domain domain = new DomainBuilder("DO_TEST", DataType.DtList).build();
		final SqlQueryResult result = executeQuery(domain, "select * from movie");

		Assert.assertEquals(3, result.getSQLRowCount());
		final DtList<Entity> dynMovies = (DtList<Entity>) result.getValue();
		Assert.assertEquals(3, dynMovies.size());

		for (final Entity dynMovie : dynMovies) {
			final long id = (Long) getValue(dynMovie, "ID");
			final String title = (String) getValue(dynMovie, "TITLE");
			checkTitle(id, title);
		}
	}

	private static Object getValue(final Entity entity, final String fieldName) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		final DtField dtField = dtDefinition.getField(fieldName);
		return dtField.getDataAccessor().getValue(entity);
	}

}
