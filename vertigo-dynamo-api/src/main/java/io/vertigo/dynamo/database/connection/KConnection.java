package io.vertigo.dynamo.database.connection;

import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.transaction.KTransactionResource;
import io.vertigo.kernel.lang.Assertion;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Connexion à une base de données JDBC.
 * Une connexion est une ressource qui participe à la transaction.
 * Le commit (ou rollback) de la transaction commit (ou rollback) les différentes
 * resources participant à la transaction puis libére (release) les différentes ressources.
 *
 * @author pchretien, npiedeloup
 */
public final class KConnection implements KTransactionResource {
	private final Connection jdbcConnection;
	private final DataBase dataBase;
	private final boolean closeable;

	/**
	 * Constructeur.
	 *
	 * @param jdbcConnection Connexion JDBC
	 * @param dataBase Base de données
	 * @param closeable Si cette connection peut-être fermée
	 * @throws SQLException Exception sql
	 */
	public KConnection(final Connection jdbcConnection, final DataBase dataBase, final boolean closeable) throws SQLException {
		Assertion.checkNotNull(jdbcConnection);
		Assertion.checkNotNull(dataBase);
		//----------------------------------------------------------------------
		this.jdbcConnection = jdbcConnection;
		this.dataBase = dataBase;
		this.closeable = closeable;
		//On ne se met jamais en mode autocommit !!
		jdbcConnection.setAutoCommit(false);
	}

	/**
	 * Retourne la connexion JDBC.
	 *
	 * @return Connexion JDBC
	 */
	public Connection getJdbcConnection() {
		return jdbcConnection;
	}

	/**
	 * @return Base de données dont est issue la connexion.
	 */
	public DataBase getDataBase() {
		return dataBase;
	}

	/** {@inheritDoc} */
	public void commit() throws SQLException {
		jdbcConnection.commit();
	}

	/** {@inheritDoc} */
	public void rollback() throws SQLException {
		jdbcConnection.rollback();
	}

	/** {@inheritDoc} */
	public void release() throws SQLException {
		if (closeable) {
			jdbcConnection.close();
		}
	}

}
