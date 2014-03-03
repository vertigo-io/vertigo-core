package io.vertigo.dynamo.impl.database.vendor.postgresql;

import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.impl.database.vendor.core.AbstractSQLExceptionHandler;
import io.vertigo.kernel.exception.VRuntimeException;

import java.sql.SQLException;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour PostgreSQL.
 * 
 * @author pforhan
 * @version $Id: PostgreSqlExceptionHandler.java,v 1.3 2013/10/22 10:43:22 pchretien Exp $
 */
final class PostgreSqlExceptionHandler extends AbstractSQLExceptionHandler {
	/**
	 * Constructeur.
	 */
	PostgreSqlExceptionHandler() {
		super();
	}

	/**
	 * Gestionnaire des erreurs de la base de données pour PostgreSql.
	 * @param sqle Exception Sql
	 * @param statement Requête en erreur.
	 */
	public void handleSQLException(final SQLException sqle, final KPreparedStatement statement) {
		final String errCode = sqle.getSQLState();
		final String code = errCode.substring(0, 2);
		if ("22001".equals(errCode) || "22003".equals(errCode)) {
			// Valeur trop grande pour ce champs
			handleTooLargeValueSqlException(sqle);
		} else if ("23503".equals(errCode)) {
			// Violation de contrainte d'intégrité référentielle
			handleForeignConstraintSQLException(sqle);
		} else if ("23505".equals(errCode)) {
			// Violation de contrainte d'unicité
			handleUniqueConstraintSQLException(sqle);
		} else if ("01".equals(code) || "02".equals(code) || "08".equals(code) || "22".equals(code) || "23".equals(code)) {
			// Erreur utilisateur
			handleUserSQLException(sqle);
		} else {
			// Message d'erreur par défaut
			handleOtherSQLException(sqle, statement);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void handleOtherSQLException(final SQLException sqle, final KPreparedStatement statement) {
		final String errCode = sqle.getSQLState();
		throw new VRuntimeException("[Erreur SQL](" + errCode + ") : " + statement, sqle);
	}

	/** {@inheritDoc} */
	@Override
	protected String extractConstraintName(final String msg) {
		final int i1 = msg.indexOf('"', msg.indexOf("constraint"));
		final int i2 = msg.indexOf('"', i1 + 1);
		if (i1 > -1 && i2 > -1 && i2 > i1) {
			return msg.substring(i1 + 1, i2).toUpperCase();
		}
		return null;
	}

}
