package io.vertigo.dynamo.impl.database.vendor.sqlserver;

import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.impl.database.vendor.core.AbstractSQLExceptionHandler;

import java.sql.SQLException;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour SQL Server.
 *
 * @author jmainaud, npiedeloup
 */
final class SqlServerExceptionHandler extends AbstractSQLExceptionHandler {

	private enum SQLServerVersion {
		PostSQLServer2008("«\u00A0", "\u00A0»."), // \u00A0 : no-break-space (ne part pas avec un trim)
		PostSQLServer2008b("\"", "\"."), // autre facon de SQL serveur pour lever les contraintes (au moins sur les FK)
		PreSQLServer2008("'", "'.");

		private final String startQuote;
		private final String endQuote;

		SQLServerVersion(final String startQuote, final String endQuote) {
			this.startQuote = startQuote;
			this.endQuote = endQuote;
		}

		public String getStartQuote() {
			return startQuote;
		}

		public String getEndQuote() {
			return endQuote;
		}
	}

	/**
	 * Constructeur.
	 */
	SqlServerExceptionHandler() {
		super();
	}

	/** {@inheritDoc} */
	public void handleSQLException(final SQLException sqle, final KPreparedStatement statement) {

		final int codeErreur = sqle.getErrorCode();

		if (codeErreur >= 20000 && codeErreur < 30000) {
			// Erreur utilisateur
			handleUserSQLException(sqle);
		} else if (codeErreur == 8152) {
			// Valeur trop grande pour ce champs (#8152)
			handleTooLargeValueSqlException(sqle);
		} else if (codeErreur == 547) {
			// Violation de contrainte d'intégrité référentielle (#547)
			handleForeignConstraintSQLException(sqle);
		} else if (codeErreur == 2627) {
			// Violation de contrainte d'unicité (#2627)
			handleUniqueConstraintSQLException(sqle);
		} else if (codeErreur == 2601) {
			// Violation de contrainte d'unicité sur index (#2601) (attention message différent)
			handleUniqueConstraintSQLException(sqle);
		} else {
			// Message d'erreur par défaut
			handleOtherSQLException(sqle, statement);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String extractConstraintName(final String messageErreur) {
		String constraintName = null;
		for (final SQLServerVersion parsingMode : SQLServerVersion.values()) {
			constraintName = extractConstraintName(parsingMode, messageErreur);
			if (constraintName != null) {
				return constraintName;
			}
		}
		return constraintName;
	}

	private String extractConstraintName(final SQLServerVersion version, final String messageErreur) {
		final int indexFin = messageErreur.indexOf(version.getEndQuote());
		if (indexFin != -1) {
			final int indexDebut = messageErreur.lastIndexOf(version.getStartQuote(), indexFin - 1);
			if (indexDebut != -1) {
				return messageErreur.substring(indexDebut + version.getStartQuote().length(), indexFin);
			}
		}
		return null;
	}
}
