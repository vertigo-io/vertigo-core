package io.vertigo.dynamo.impl.database;

import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.impl.database.statement.StatementStats;

/**
* Interface de réception des  événements produits par l'exécution des taches SQL.
*
* @author pchretien
*/
public interface DataBaseListener {
	/**
	 * Enregistre le début d'exécution d'un PreparedStatement.
	 * @param preparedStatement Statement
	 */
	void onPreparedStatementStart(KPreparedStatement preparedStatement);

	/**
	 * Enregistre la fin d'une exécution de PreparedStatement avec le temps d'exécution en ms et son statut (OK/KO).
	 * @param statementStats Informations sur l'éxécution
	 */
	void onPreparedStatementFinish(StatementStats statementStats);
}
