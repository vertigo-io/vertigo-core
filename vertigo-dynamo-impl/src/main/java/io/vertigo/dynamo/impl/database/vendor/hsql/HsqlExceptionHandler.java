package io.vertigo.dynamo.impl.database.vendor.hsql;

import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.impl.database.vendor.core.AbstractSQLExceptionHandler;

import java.sql.SQLException;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour HSQL.
 * @author dchallas
 */
final class HsqlExceptionHandler extends AbstractSQLExceptionHandler {
	/**
	 * Constructeur.
	 */
	HsqlExceptionHandler() {
		super();
	}

	/** {@inheritDoc} */
	public void handleSQLException(final SQLException sqle, final KPreparedStatement statement) {
		// Message d'erreur par défaut
		handleOtherSQLException(sqle, statement);
		// voir les codes dans org.hsqldb.Trace
	}

	@Override
	protected String extractConstraintName(final String msg) {
		// TODO Auto-generated method stub
		// voir les codes dans org.hsqldb.Trace
		return msg;
	}

}
