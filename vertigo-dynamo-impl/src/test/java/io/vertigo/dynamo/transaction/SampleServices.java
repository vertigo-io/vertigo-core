package io.vertigo.dynamo.transaction;

import io.vertigo.dynamo.transaction.data.SampleDataBase;
import io.vertigo.dynamo.transaction.data.SampleDataBaseConnection;
import io.vertigo.dynamo.transaction.data.SampleTransactionResource;
import io.vertigo.lang.Component;

import javax.inject.Inject;

import org.junit.Assert;

public class SampleServices implements Component {
	private static int count;

	@Inject
	private VTransactionManager transactionManager;
	private final SampleDataBase dataBase = new SampleDataBase();

	private SampleDataBaseConnection obtainDataBaseConnection(final SampleDataBase sampleDataBase, final String resourceId) {
		// --- resource 1
		final VTransactionResourceId<SampleTransactionResource> transactionResourceId = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, resourceId);

		final SampleTransactionResource transactionResourceMock = new SampleTransactionResource(sampleDataBase);
		transactionManager.getCurrentTransaction().addResource(transactionResourceId, transactionResourceMock);
		return transactionResourceMock;
	}

	@Transactional
	public String test() {
		final SampleDataBaseConnection connection = obtainDataBaseConnection(dataBase, "test-memory-1");

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
