package io.vertigo.dynamo.database.vendor.oracle;

import org.junit.Ignore;

import io.vertigo.dynamo.database.AbstractSqlDataBaseManagerTest;
import io.vertigo.dynamo.database.connection.SqlConnection;

@Ignore
public class OracleDataBaseManagerTest extends AbstractSqlDataBaseManagerTest {

	@Override
	protected void doSetUp() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			//  BIGINT Datatype used on Abstract class is incompatible with Oracle so we use number
			execCallableStatement(connection, "create table movie(id NUMBER , title varchar(255))");
		} finally {
			connection.release();
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		final SqlConnection connection = obtainMainConnection();
		try {
			// we use a shared database so we need to drop the table
			execCallableStatement(connection, "drop table movie");
		} finally {
			connection.release();
		}

	}

}
