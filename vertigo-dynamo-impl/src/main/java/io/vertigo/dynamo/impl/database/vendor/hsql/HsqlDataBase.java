package io.vertigo.dynamo.impl.database.vendor.hsql;

import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.database.vendor.SQLExceptionHandler;
import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.impl.database.vendor.core.SQLMappingImpl;

/**
 * Gestion de la base de données HSQL.
 * 
 * @author pchretien
 */
public final class HsqlDataBase implements DataBase {
	private final SQLExceptionHandler sqlExceptionHandler = new HsqlExceptionHandler();
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
