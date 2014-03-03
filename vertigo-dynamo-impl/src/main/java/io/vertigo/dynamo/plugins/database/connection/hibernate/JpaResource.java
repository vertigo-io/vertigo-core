package io.vertigo.dynamo.plugins.database.connection.hibernate;

import io.vertigo.dynamo.transaction.KTransactionResource;
import io.vertigo.kernel.lang.Assertion;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * Les "transactions" hibernate sont considérées comme une ressource d'une
 * transaction plus globale pouvant gérer d'autres ressources comme des mails,
 * des fichiers...
 *
 * @author pchretien
 * @version $Id: JpaResource.java,v 1.1 2014/01/31 17:21:08 npiedeloup Exp $
 */
public class JpaResource implements KTransactionResource {
	private final EntityManager em;
	private final EntityTransaction tx;

	/**
	 * Constructeur.
	 *
	 * @param entityManagerFactory EntityManagerFactory
	 */
	public JpaResource(final EntityManagerFactory entityManagerFactory) {
		em = entityManagerFactory.createEntityManager();
		tx = em.getTransaction();
		// ---------------------------------------------------------------------
		Assertion.checkNotNull(tx);
		Assertion.checkNotNull(em);
		// ---------------------------------------------------------------------
		tx.begin();
	}

	/** {@inheritDoc} */
	public void commit() {
		tx.commit();
	}

	/** {@inheritDoc} */
	public void rollback() {
		tx.rollback();
	}

	/** {@inheritDoc} */
	public void release() {
		em.close();
	}

	/**
	 * @return EntityManager hibernate lié à la ressource
	 */
	public final EntityManager getEntityManager() {
		return em;
	}
}
