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

import org.apache.log4j.Logger;

/**
 * Réception des  événements produits lors de l'exécution des transactions.
 * 
 * @author pchretien
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
