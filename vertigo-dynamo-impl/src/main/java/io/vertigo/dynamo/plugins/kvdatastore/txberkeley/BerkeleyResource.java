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
package io.vertigo.dynamo.plugins.kvdatastore.txberkeley;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.transaction.KTransactionResource;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

/**
 * Gestion d'une connexion à une Base Berkeley.
 * Permet de créer un writer et un reader géré par la transaction porteuse de la ressource.
 *
 * @author pchretien
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
			throw new RuntimeException(e);
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
