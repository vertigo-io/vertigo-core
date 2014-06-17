package io.vertigo.dynamo.impl.database.vendor.h2;

import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.database.vendor.SQLExceptionHandler;
import io.vertigo.dynamo.database.vendor.SQLMapping;

/**
 * Gestion de la base de données H2.
 * 
 * @author jmainaud
 */
public final class H2Database implements DataBase {
	private final SQLExceptionHandler sqlExceptionHandler = new H2SqlExceptionHandler();
	private final SQLMapping sqlMapping = new H2Mapping();

	/** {@inheritDoc} */
	public SQLExceptionHandler getSqlExceptionHandler() {
		return sqlExceptionHandler;
	}

	/** {@inheritDoc} */
	public SQLMapping getSqlMapping() {
		return sqlMapping;
	}
}
