package io.vertigo.dynamo.impl.database.vendor.oracle;

import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.impl.database.vendor.core.AbstractSQLExceptionHandler;

import java.sql.SQLException;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour Oracle.
 * 
 * @author npiedeloup
 * @version $Id: OracleExceptionHandler.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
final class OracleExceptionHandler extends AbstractSQLExceptionHandler {
	/**
	 * Constructeur.
	 */
	OracleExceptionHandler() {
		super();
	}

	/** {@inheritDoc} */
	public void handleSQLException(final SQLException sqle, final KPreparedStatement statement) {
		final int errCode = sqle.getErrorCode();
		if (errCode >= 20000 && errCode < 30000) {
			// Erreur utilisateur
			handleUserSQLException(sqle);
		} else if (errCode == 1438 || errCode == 12899) {
			// Valeur trop grande pour ce champs
			handleTooLargeValueSqlException(sqle);
		} else if (errCode == 2292) {
			// Violation de contrainte d'intégrité référentielle
			handleForeignConstraintSQLException(sqle);
		} else if (errCode == 1) {
			// Violation de contrainte d'unicité
			handleUniqueConstraintSQLException(sqle);
		} else {
			// Message d'erreur par défaut
			handleOtherSQLException(sqle, statement);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String extractConstraintName(final String msg) {
		final int i1 = msg.indexOf('.', msg.indexOf('('));
		final int i2 = msg.indexOf(')', i1);
		if (i1 > -1 && i2 > -1) {
			return msg.substring(i1 + 1, i2);
		}
		return null;
	}
}
