package io.vertigo.dynamo.database;

import io.vertigo.dynamo.database.connection.ConnectionProvider;
import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.statement.KCallableStatement;
import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.kernel.component.Manager;

/**
* Gestionnaire des accès aux bases de données.
*
* @author pchretien
*/
public interface DataBaseManager extends Manager {
	/**
	 * @return ConnectionProvider
	 */
	ConnectionProvider getConnectionProvider();

	/**
	 * @param connection Connexion
	 * @param procName  Nom de la procédure
	 * @return statement
	 */
	KCallableStatement createCallableStatement(final KConnection connection, final String procName);

	/**
	 * @param connection Connexion
	 * @param sql Requête SQL
	 * @param generatedKeys Si on récupère les clés générées par la base de données.
	 * @return Statement
	 */
	KPreparedStatement createPreparedStatement(final KConnection connection, final String sql, final boolean generatedKeys);

}
