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
package io.vertigo.dynamo.transaction.database;

import io.vertigo.dynamo.transaction.KTransactionResource;
import io.vertigo.lang.Assertion;

/**
 *
 * @author dchallas
 *
 */
public final class TransactionResourceMock implements KTransactionResource, IDataBaseMock {
	private final DataBaseMock bdd;
	private String dataUpdated;
	private boolean isUpdated;

	private enum State {
		Started, Closed
	}

	private State state = State.Started;

	public TransactionResourceMock(final DataBaseMock bdd) {
		Assertion.checkNotNull(bdd);
		//-----
		this.bdd = bdd;
	}

	/** {@inheritDoc} */
	@Override
	public void commit() {
		check();
		if (isUpdated) {
			bdd.setData(dataUpdated);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		check();
		dataUpdated = null;
		isUpdated = false;
		state = State.Closed;
	}

	private void check() {
		Assertion.checkArgument(state == State.Started, "Ressource fermée.");
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() {
		check();
		//Pas de mise à jour
	}

	@Override
	public void setData(final String newdata) {
		check();
		this.isUpdated = true;
		this.dataUpdated = newdata;
	}

	@Override
	public String getData() {
		check();
		return isUpdated ? dataUpdated : bdd.getData();
	}
}
