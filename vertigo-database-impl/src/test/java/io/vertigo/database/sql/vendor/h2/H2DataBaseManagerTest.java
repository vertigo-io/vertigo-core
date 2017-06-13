package io.vertigo.database.sql.vendor.h2;

import io.vertigo.database.sql.AbstractSqlDataBaseManagerTest;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;

public final class H2DataBaseManagerTest extends AbstractSqlDataBaseManagerTest {
	private static final String CREATE_TABLE_MOVIE = "CREATE TABLE movie ( "
			+ "id 						NUMBER(6), "
			+ "title 					VARCHAR2(255), "
			+ "fps 						NUMBER(6,3), "
			+ "income 					NUMBER(6,3), "
			+ "color 					BOOLEAN, "
			+ "release_date 			TIMESTAMP, "
			+ "release_local_date 		DATE, "
			+ "release_zoned_date_time 	TIMESTAMP, "
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
				e.printStackTrace(System.out);
			}
			execpreparedStatement(connection, DROP_TABLE_MOVIE);
		} finally {
			connection.release();
		}
	}

	@Override
	protected GenerationMode getExpectedGenerationMode() {
		return GenerationMode.GENERATED_KEYS;
	}
}
