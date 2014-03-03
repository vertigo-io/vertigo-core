package io.vertigo.dynamo.impl.database.vendor.sqlserver;

import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.database.vendor.SQLExceptionHandler;
import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.impl.database.vendor.core.SQLMappingImpl;

/**
 * Gestion de la base de données SqlServer.
 * 
 * @author pchretien
 * @version $Id: SqlServerDataBase.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public final class SqlServerDataBase implements DataBase {
	private final SQLExceptionHandler sqlExceptionHandler = new SqlServerExceptionHandler();
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
