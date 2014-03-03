package io.vertigo.dynamo.plugins.kvdatastore.berkeley;

import io.vertigo.dynamo.transaction.KTransactionResource;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

/**
 * Gestion d'une connexion à une Base Berkeley.
 * Permet de créer un writer et un reader géré par la transaction porteuse de la ressource.
 *
 * @author pchretien
 * @version $Id: BerkeleyResource.java,v 1.1 2013/01/02 13:38:30 pchretien Exp $
 */
final class BerkeleyResource implements KTransactionResource {
	private Transaction transaction;

	/***
	 * Constructeur.
	 * @param database Base Berkeley DB.
	 */
	BerkeleyResource(final Database database) {
		Assertion.checkNotNull(database);
		//======================================================================
		try {
			transaction = database.getEnvironment().beginTransaction(null, null);
		} catch (final DatabaseException e) {
			throw new VRuntimeException(e);
		}
	}

	Transaction getBerkeleyTransaction() {
		return transaction;
	}

	/** {@inheritDoc} */
	public void commit() throws Exception {
		if (transaction != null) {
			transaction.commit();
		}
	}

	/** {@inheritDoc} */
	public void rollback() throws Exception {
		if (transaction != null) {
			transaction.abort();
		}
	}

	/** {@inheritDoc} */
	public void release() {
		if (transaction != null) {
			transaction = null;
		}
	}
}
