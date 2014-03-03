package io.vertigo.dynamo.transaction.database;

import io.vertigo.dynamo.transaction.KTransactionResource;
import io.vertigo.kernel.lang.Assertion;

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
		//---------------------------------------------------------------------
		this.bdd = bdd;
	}

	/** {@inheritDoc} */
	public void commit() {
		check();
		if (isUpdated) {
			bdd.setData(dataUpdated);
		}
	}

	/** {@inheritDoc} */
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
	public void rollback() {
		check();
		//Pas de mise à jour
	}

	public void setData(final String newdata) {
		check();
		this.isUpdated = true;
		this.dataUpdated = newdata;
	}

	public String getData() {
		check();
		return isUpdated ? dataUpdated : bdd.getData();
	}
}
