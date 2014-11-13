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

import io.vertigo.dynamo.impl.transaction.listener.KTransactionListener;
import io.vertigo.dynamo.impl.transaction.listener.KTransactionListenerImpl;
import io.vertigo.dynamo.transaction.KTransaction;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.lang.Assertion;

/**
 * Implémentation standard du gestionnaire de transactions.
 *
 * @author  pchretien
 */
public final class KTransactionManagerImpl implements KTransactionManager {
	private final KTransactionListener transactionListener = new KTransactionListenerImpl();

	/** {@inheritDoc} */
	@Override
	public KTransaction getCurrentTransaction() {
		return getCurrentTransactionImpl();
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasCurrentTransaction() {
		return KTransactionImpl.getLocalCurrentTransaction() != null;
	}

	/** {@inheritDoc} */
	@Override
	public KTransactionWritable createCurrentTransaction() {
		//Il faut qu'il n'existe aucune transaction en cours.
		if (hasCurrentTransaction()) {
			throw new IllegalStateException("current transaction already created");
		}
		//On démarre la Transaction à cet endroit précis.
		return new KTransactionImpl(transactionListener);
	}

	/** {@inheritDoc} */
	@Override
	public KTransactionWritable createAutonomousTransaction() {
		final KTransactionImpl currentTransaction = getCurrentTransactionImpl();
		return new KTransactionImpl(currentTransaction);
	}

	/**
	 * Retourne la transaction courante (forcément non null).
	 * @return Transaction courante (la plus basse)
	 */
	private static KTransactionImpl getCurrentTransactionImpl() {
		final KTransactionImpl transaction = KTransactionImpl.getLocalCurrentTransaction();
		Assertion.checkNotNull(transaction, "current transaction not found");
		return transaction.getDeepestTransaction();
	}
}
