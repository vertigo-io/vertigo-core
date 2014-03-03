package io.vertigo.dynamo.impl.transaction;

import org.apache.log4j.Logger;

/**
 * Réception des  événements produits lors de l'exécution des transactions.
 * 
 * @author pchretien
 * @version $Id: KTransactionListener.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
final class KTransactionListener {
	private static final String MS = " ms)";

	/**
	 * Mécanisme de log utilisé pour les transactions
	 */
	private final Logger transactionLog;

	/**
	 * Constructeur.
	 */
	KTransactionListener() {
		transactionLog = Logger.getLogger("transaction");
	}

	/**
	 * Enregistre le début d'une transaction.
	 */
	void onTransactionStart() {
		if (transactionLog.isTraceEnabled()) {
			transactionLog.trace("Demarrage de la transaction");
		}
	}

	/**
	 * Fin de transaction.
	 * @param rollback Si la transaction a réussie ou rollbackée
	 * @param elapsedTime Temps d'exécution en ms
	 */
	void onTransactionFinish(final boolean rollback, final long elapsedTime) {
		if (transactionLog.isTraceEnabled()) {
			if (rollback) {
				transactionLog.trace(">>Transaction rollback en ( " + elapsedTime + MS);
			} else {
				transactionLog.trace(">>Transaction commit en ( " + elapsedTime + MS);
			}
		}
	}
}
