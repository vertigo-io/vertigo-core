package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.database.statement.KPreparedStatement;

/**
* Interface de statistiques pour le suivi des traitements SQL.
*
* @author npiedeloup
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
