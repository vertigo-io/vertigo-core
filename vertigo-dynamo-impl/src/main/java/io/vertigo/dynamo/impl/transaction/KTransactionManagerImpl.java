package io.vertigo.dynamo.impl.transaction;

import io.vertigo.dynamo.transaction.KTransaction;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation standard du gestionnaire de transactions.
 *
 * @author  pchretien
 */
public final class KTransactionManagerImpl implements KTransactionManager {
	/**
	 * Contient la transaction du Thread courant.
	 */
	private final ThreadLocal<KTransactionImpl> currentThreadLocalTransaction = new ThreadLocal<>();
	private final KTransactionListener transactionListener = new KTransactionListener();

	/** {@inheritDoc} */
	public KTransaction getCurrentTransaction() {
		return getCurrentTransactionStandard();
	}

	/** {@inheritDoc} */
	public boolean hasCurrentTransaction() {
		return getLocalCurrentTransaction() != null;
	}

	/** {@inheritDoc} */
	public KTransactionWritable createCurrentTransaction() {
		//Il faut qu'il n'existe aucune transaction en cours.
		if (hasCurrentTransaction()) {
			throw new IllegalStateException("Transaction courante déjà créée");
		}
		//On démarre la Transaction à cet endroit précis.
		final KTransactionImpl transaction = new KTransactionImpl(transactionListener);
		currentThreadLocalTransaction.set(transaction);
		return transaction;
	}

	/** {@inheritDoc} */
	public KTransactionWritable createAutonomousTransaction() {
		final KTransactionImpl currentTransaction = getCurrentTransactionStandard();
		return new KTransactionImpl(currentTransaction);
	}

	//==========================================================================
	//=========================PRIVATE==========================================
	//==========================================================================

	/**
	 * Retourne la transaction courante (forcément non null).
	 * @return Transaction courante (la plus basse)
	 */
	private KTransactionImpl getCurrentTransactionStandard() {
		final KTransactionImpl transaction = getLocalCurrentTransaction();
		Assertion.checkNotNull(transaction, "Pas de transaction courante");
		return transaction.getDeepestTransaction();
	}

	/**
	 * Retourne la transaction courante de plus haut niveau.
	 * - jamais closed
	 * - peut être null
	 * @return KTransaction
	 */
	private KTransactionImpl getLocalCurrentTransaction() {
		KTransactionImpl transaction = currentThreadLocalTransaction.get();
		//Si la transaction courante est finie on ne la retourne pas.
		if (transaction != null && transaction.isClosed()) {
			transaction = null;
		}
		return transaction;
	}
}
