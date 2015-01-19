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
package io.vertigo.dynamo.impl.transaction.listener;

import org.apache.log4j.Logger;

/**
 * Réception des  événements produits lors de l'exécution des transactions.
 *
 * @author pchretien
 */
public final class KTransactionListenerImpl implements KTransactionListener {
	private static final String MS = " ms)";

	/**
	 * Mécanisme de log utilisé pour les transactions
	 */
	private static final Logger LOGGER = Logger.getLogger("transaction");

	/** {@inheritDoc} */
	@Override
	public void onTransactionStart() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Start transaction");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onTransactionFinish(final boolean rollback, final long elapsedTime) {
		if (LOGGER.isTraceEnabled()) {
			if (rollback) {
				LOGGER.trace(">>Transaction rollback ( " + elapsedTime + MS);
			} else {
				LOGGER.trace(">>Transaction commit ( " + elapsedTime + MS);
			}
		}
	}
}
