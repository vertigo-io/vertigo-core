package io.vertigo.database.sql.vendor.oracle;

import org.junit.Ignore;

import io.vertigo.database.sql.AbstractSqlDataBaseManagerTest;
import io.vertigo.database.sql.connection.SqlConnection;

@Ignore
public class OracleDataBaseManagerTest extends AbstractSqlDataBaseManagerTest {

	@Override
	protected void doSetUp() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			//  BIGINT Datatype used on Abstract class is incompatible with Oracle so we use number
			execpreparedStatement(connection, "create table movie(id NUMBER , title varchar(255))");
		} finally {
			connection.release();
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			// we use a shared database so we need to drop the table
			execpreparedStatement(connection, "drop table movie");
		} finally {
			connection.release();
		}

	}

}
