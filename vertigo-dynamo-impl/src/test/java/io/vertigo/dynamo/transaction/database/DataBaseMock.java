package io.vertigo.dynamo.transaction.database;

/**
 * 
 * @author dchallas
 *
 */
public final class DataBaseMock /*implements IDataBaseMock*/{
	private String data;

	public DataBaseMock() {
		super();
	}

	void setData(final String newdata) {
		this.data = newdata;
	}

	public String getData() {
		return data;
	}
}
