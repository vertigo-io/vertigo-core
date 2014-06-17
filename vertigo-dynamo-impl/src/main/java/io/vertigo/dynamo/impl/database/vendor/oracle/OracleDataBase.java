package io.vertigo.dynamo.impl.database.vendor.oracle;

import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.database.vendor.SQLExceptionHandler;
import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.impl.database.vendor.core.SQLMappingImpl;

/**
 * Gestiond de la base de données Oracle.
 * 
 * @author pchretien
 */
public final class OracleDataBase implements DataBase {
	private final SQLExceptionHandler sqlExceptionHandler = new OracleExceptionHandler();
	private final SQLMapping sqlMapping = new SQLMappingImpl();

	/** {@inheritDoc} */
	public SQLExceptionHandler getSqlExceptionHandler() {
		return sqlExceptionHandler;
	}

	/** {@inheritDoc} */
	public SQLMapping getSqlMapping() {
		return sqlMapping;
	}
}
