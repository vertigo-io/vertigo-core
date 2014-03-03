package io.vertigo.dynamo.transaction.database;

/**
 * bouchon de test.
 * @author dchallas
 *
 */
public interface IDataBaseMock {

	void setData(final String data);

	String getData();
}
