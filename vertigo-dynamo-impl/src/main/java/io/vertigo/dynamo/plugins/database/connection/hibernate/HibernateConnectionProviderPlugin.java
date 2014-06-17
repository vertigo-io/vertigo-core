package io.vertigo.dynamo.plugins.database.connection.hibernate;

import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.transaction.KTransactionManager;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;

/**
 * ConnectionProvider permettant la connexion à une datasource Java.
 *
 * @author pchretien, npiedeloup
 */
public final class HibernateConnectionProviderPlugin extends JpaConnectionProviderPlugin {

	/**
	 * Constructeur.
	 * @param dataBaseName Nom du type de base de données
	 * @param persistenceUnit Nom de la persistenceUnit à utiliser (dans le persistence.xml)
	 */
	@Inject
	public HibernateConnectionProviderPlugin(@Named("persistenceUnit") final String persistenceUnit, @Named("dataBaseName") final String dataBaseName, final KTransactionManager transactionManager) {
		super(persistenceUnit, dataBaseName, transactionManager);
	}

	/** {@inheritDoc} */
	@Override
	public KConnection obtainWrappedConnection(final EntityManager em) {
		//preconisation StackOverFlow to get current jpa connection
		final Session session = em.unwrap(Session.class);
		return session.doReturningWork(new ReturningWork<KConnection>() {
			public KConnection execute(final Connection connection) throws SQLException {
				return new KConnection(connection, getDataBase(), false);
			}
		});
	}
}
