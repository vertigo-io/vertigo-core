package io.vertigo.database.sql.vendor.oracle;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.database.sql.AbstractSqlDataBaseManagerTest;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.statement.SqlParameter;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;

public final class OracleDataBaseManagerTest extends AbstractSqlDataBaseManagerTest {
	private static final String CREATE_TABLE_MOVIE = "CREATE TABLE movie ( "
			+ "id 						NUMBER(6), "
			+ "title 					VARCHAR2(255), "
			+ "fps 						NUMBER(6,3), "
			+ "income 					NUMBER(6,3), "
			+ "color 					NUMBER(1), "
			+ "release_date 			DATE, "
			+ "release_local_date 		DATE, "
			+ "release_zoned_date_time 	DATE, "
			+ "icon 					BLOB"
			+ ")";
	private static final String CREATE_SEQUENCE_MOVIE = "CREATE SEQUENCE seq_movie";

	private static final String DROP_TABLE_MOVIE = "DROP TABLE movie";
	private static final String DROP_SEQUENCE_MOVIE = "DROP SEQUENCE seq_movie";

	@Override
	protected final void doSetUp() throws Exception {
		//A chaque test on recrée la table famille
		final SqlConnection connection = obtainMainConnection();
		try {
			execpreparedStatement(connection, CREATE_TABLE_MOVIE);
			execpreparedStatement(connection, CREATE_SEQUENCE_MOVIE);
		} finally {
			connection.release();
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			// we use a shared database so we need to drop the table
			try {
				execpreparedStatement(connection, DROP_SEQUENCE_MOVIE);
			} catch (final Exception e) {
				//e.printStackTrace(System.out);
			}
			execpreparedStatement(connection, DROP_TABLE_MOVIE);
		} finally {
			connection.release();
		}
	}

	@Test
	public void testInsert() throws Exception {
		final String insertWithgeneratedKey = obtainMainConnection().getDataBase().getSqlDialect()
				.createInsertQuery(
						"ID",
						Arrays.asList("TITLE"),
						"seq_",
						"movie");

		final GenerationMode generationMode = obtainMainConnection().getDataBase().getSqlDialect().getGenerationMode();
		//We check that Oracle is in the right expected mode
		Assert.assertEquals(GenerationMode.GENERATED_COLUMNS, generationMode);
		//---
		final SqlConnection connection = obtainMainConnection();
		try {
			final String sql = dataBaseManager.parseQuery(insertWithgeneratedKey).getVal1();

			dataBaseManager.createPreparedStatement(connection).executeUpdateWithGeneratedKey(
					sql,
					Arrays.asList(SqlParameter.of(String.class, "frankenstein")),
					GenerationMode.GENERATED_COLUMNS,
					"ID",
					Long.class);
			connection.commit();
		} finally {
			connection.release();
		}
		final List<Integer> result = executeQuery(Integer.class, "select count(*) from movie", null);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(1, result.get(0).intValue());
	}

}
