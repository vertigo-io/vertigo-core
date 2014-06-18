package io.vertigo.dynamo.plugins.database.connection.hibernate;

import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.database.vendor.SQLExceptionHandler;
import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.transaction.KTransaction;
import io.vertigo.dynamo.transaction.KTransactionResourceId;
import io.vertigo.kernel.lang.Assertion;

import javax.persistence.EntityManagerFactory;

/**
 * Gestion de la base de données Hibernate.
 * 
 * @author npiedeloup
 */
public final class JpaDataBase implements DataBase {
	//This Resource must be commited AFTER the KConnection ones. The release of EntityManager close the DB Connection and KConnection can't be commited anymore
	private static final KTransactionResourceId<JpaResource> JPA_RESOURCE_ID = new KTransactionResourceId<>(KTransactionResourceId.Priority.NORMAL, "Jpa");

	private final DataBase innerDataBase;
	private final EntityManagerFactory entityManagerFactory;

	/**
	 * Constructeur.
	 * @param innerDataBase Base sous jacente
	 * @param entityManagerFactory entityManagerFactory
	 */
	JpaDataBase(final DataBase innerDataBase, final EntityManagerFactory entityManagerFactory) {
		Assertion.checkNotNull(innerDataBase);
		Assertion.checkNotNull(entityManagerFactory);
		//---------------------------------------------------------------------
		this.innerDataBase = innerDataBase;
		this.entityManagerFactory = entityManagerFactory;
	}

	/** {@inheritDoc} */
	public SQLExceptionHandler getSqlExceptionHandler() {
		return innerDataBase.getSqlExceptionHandler();
	}

	/** {@inheritDoc} */
	public SQLMapping getSqlMapping() {
		return innerDataBase.getSqlMapping();
	}

	/** 
	 * récupère la ressource JPA de la transaction et la créé si nécessaire. 
	 * @param transaction Transaction courante
	 * @return ResourceJpa de la transaction, elle est crée si nécessaire.
	 * */
	public JpaResource obtainJpaResource(final KTransaction transaction) {
		JpaResource resource = transaction.getResource(JPA_RESOURCE_ID);

		if (resource == null) {
			// Si aucune ressource de type JPA existe sur la transaction, on la créé
			resource = new JpaResource(entityManagerFactory);
			transaction.addResource(JPA_RESOURCE_ID, resource);
		}
		return resource;
	}
}
