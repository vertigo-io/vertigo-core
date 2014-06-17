package io.vertigo.dynamo.plugins.database.connection.hibernate;

import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.plugins.database.connection.AbstractConnectionProviderPlugin;
import io.vertigo.dynamo.transaction.KTransaction;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * ConnectionProvider permettant la connexion à une datasource Java.
 *
 * @author pchretien, npiedeloup
 */
abstract class JpaConnectionProviderPlugin extends AbstractConnectionProviderPlugin {
	private final KTransactionManager transactionManager;

	/**
	 * Constructeur.
	 * @param dataBaseName Nom du type de base de données
	 * @param persistenceUnit Nom de la persistenceUnit à utiliser (dans le persistence.xml)
	 */
	@Inject
	public JpaConnectionProviderPlugin(@Named("persistenceUnit") final String persistenceUnit, @Named("dataBaseName") final String dataBaseName, final KTransactionManager transactionManager) {
		super(new JpaDataBase(createDataBase(dataBaseName), Persistence.createEntityManagerFactory(persistenceUnit)));
		Assertion.checkArgNotEmpty(persistenceUnit);
		//---------------------------------------------------------------------
		this.transactionManager = transactionManager;
	}

	/** {@inheritDoc} */
	public final KConnection obtainConnection() throws SQLException {
		final EntityManager em = obtainJpaResource().getEntityManager();
		return obtainWrappedConnection(em);
	}

	/**
	 * @param em EntityManager
	 * @return KConnection sous jacente
	 * @throws SQLException Exception sql
	 */
	protected abstract KConnection obtainWrappedConnection(final EntityManager em) throws SQLException;

	/** récupère la ressource JPA de la transaction et la créé si nécessaire. */
	private JpaResource obtainJpaResource() {
		final DataBase dataBase = getDataBase();
		Assertion.checkState(dataBase instanceof JpaDataBase, "DataBase must be a JpaDataBase (current:{0}).", dataBase.getClass());
		return ((JpaDataBase) dataBase).obtainJpaResource(getCurrentTransaction());
	}

	/** récupère la transaction courante. */
	private KTransaction getCurrentTransaction() {
		return transactionManager.getCurrentTransaction();
	}

	private static DataBase createDataBase(final String dataBaseName) {
		return ClassUtil.newInstance(dataBaseName, DataBase.class);
	}
}
