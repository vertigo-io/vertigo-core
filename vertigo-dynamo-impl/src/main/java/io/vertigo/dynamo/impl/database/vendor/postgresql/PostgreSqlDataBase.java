package io.vertigo.dynamo.impl.database.vendor.postgresql;

import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.database.vendor.SQLExceptionHandler;
import io.vertigo.dynamo.database.vendor.SQLMapping;

/**
 * Gestiond de la base de données PostrgreSQL.
 * 
 * @author pchretien
 */
public final class PostgreSqlDataBase implements DataBase {
	private final SQLExceptionHandler sqlExceptionHandler = new PostgreSqlExceptionHandler();
	private final SQLMapping sqlMapping = new PostgresqlMapping();

	/** {@inheritDoc} */
	public SQLExceptionHandler getSqlExceptionHandler() {
		return sqlExceptionHandler;
	}

	/** {@inheritDoc} */
	public SQLMapping getSqlMapping() {
		return sqlMapping;
	}
}
