package io.vertigo.dynamo.database.connection;

import io.vertigo.dynamo.database.vendor.DataBase;

import java.sql.SQLException;

/**
 * Fournisseur de connexions KConnection.
 *
 * @author pchretien
 * @version $Id: ConnectionProvider.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface ConnectionProvider {
	/**
	 * Retourne une connexion.
	 *
	 * @return Connexion
	 * @throws SQLException Exception sql
	 */
	KConnection obtainConnection() throws SQLException;

	/**
	 * @return Type de base de données
	 */
	DataBase getDataBase();
}
