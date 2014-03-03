package io.vertigo.dynamo.database.vendor;

import io.vertigo.dynamo.database.statement.KPreparedStatement;

import java.sql.SQLException;

/**
 * Handler des exceptions SQL qui peuvent survenir lors de l'exécution d'une requête.
 * @author npiedeloup
 * @version $Id: SQLExceptionHandler.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface SQLExceptionHandler {

	/**
	 * Gestion des erreurs SQL => Transformation en erreurs KSystemException et KUserException
	 * selon la plage de l'erreur.
	 * @param sqle Exception survenue
	 * @param statement Statement SQL (i.e. requête SQL)
	 */
	void handleSQLException(SQLException sqle, KPreparedStatement statement);
}
