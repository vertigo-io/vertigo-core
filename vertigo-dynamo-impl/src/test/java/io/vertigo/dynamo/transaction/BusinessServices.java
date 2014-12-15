package io.vertigo.dynamo.transaction;

import io.vertigo.dynamo.transaction.database.DataBaseMock;
import io.vertigo.dynamo.transaction.database.IDataBaseMock;
import io.vertigo.dynamo.transaction.database.TransactionResourceMock;
import io.vertigo.lang.Component;

import javax.inject.Inject;

import org.junit.Assert;

public class BusinessServices implements Component {
	private static int count;

	@Inject
	private KTransactionManager transactionManager;
	private final DataBaseMock dataBase = new DataBaseMock();

	private IDataBaseMock obtainDataBaseConnection(final DataBaseMock myDataBase, final String resourceId) {
		// --- resource 1
		final KTransactionResourceId<TransactionResourceMock> transactionResourceId = new KTransactionResourceId<>(KTransactionResourceId.Priority.TOP, resourceId);

		final TransactionResourceMock transactionResourceMock = new TransactionResourceMock(myDataBase);
		transactionManager.getCurrentTransaction().addResource(transactionResourceId, transactionResourceMock);
		return transactionResourceMock;
	}

	@Transactional
	public String test() {
		final IDataBaseMock connection = obtainDataBaseConnection(dataBase, "test-memory-1");

		// --- modification de la bdd
		final String value = createNewData();
		connection.setData(value);
		Assert.assertEquals(value, connection.getData());
		return value;
	}

	public void check(final String value) {
		//On vérifie que la bdd est mise à jour.
		Assert.assertEquals(value, dataBase.getData());
	}

	private static String createNewData() {
		count++;
		return "data - [" + count + "]" + String.valueOf(System.currentTimeMillis());
	}

}
