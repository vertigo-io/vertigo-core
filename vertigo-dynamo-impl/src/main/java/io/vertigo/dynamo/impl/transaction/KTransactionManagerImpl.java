/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
