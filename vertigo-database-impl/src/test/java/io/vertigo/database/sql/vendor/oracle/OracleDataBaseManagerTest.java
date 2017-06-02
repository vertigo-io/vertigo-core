package io.vertigo.database.sql.vendor.oracle;

import io.vertigo.database.sql.AbstractSqlDataBaseManagerTest;
import io.vertigo.database.sql.connection.SqlConnection;

public final class OracleDataBaseManagerTest extends AbstractSqlDataBaseManagerTest {
	@Override
	protected String createTableMovie() {
		return "CREATE TABLE movie ( "
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
	}

	@Override
	protected void doTearDown() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			// we use a shared database so we need to drop the table
			execpreparedStatement(connection, "DROP TABLE movie");
		} finally {
			connection.release();
		}

	}
}
