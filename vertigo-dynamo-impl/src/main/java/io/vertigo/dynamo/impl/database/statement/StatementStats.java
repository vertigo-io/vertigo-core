package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.database.statement.KPreparedStatement;

/**
* Interface de statistiques pour le suivi des traitements SQL.
*
* @author npiedeloup
* @version $Id: StatementStats.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
*/
public interface StatementStats {
	/**
	 * @return preparedStatement Statement
	 */
	KPreparedStatement getPreparedStatement();

	/**
	 * @return elapsedTime Temps d'exécution en ms
	 */
	long getElapsedTime();

	/**
	 * @return Nombre de lignes affectées (update, insert, delete), null si sans objet
	 */
	Long getNbModifiedRow();

	/**
	 * @return Nombre de lignes récupérées (select), null si sans objet
	 */
	Long getNbSelectedRow();

	/**
	 * @return success Si l'exécution a réussi
	 */
	boolean isSuccess();
}
