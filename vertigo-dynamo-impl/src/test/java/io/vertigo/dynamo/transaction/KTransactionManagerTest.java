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
package io.vertigo.dynamo.transaction;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.transaction.data.SampleDataBase;
import io.vertigo.dynamo.transaction.data.SampleDataBaseConection;
import io.vertigo.dynamo.transaction.data.SampleTransactionResource;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author dchallas
 *
 */
public final class KTransactionManagerTest extends AbstractTestCaseJU4 {
	private static int count;

	@Inject
	private BusinessServices businessServices;

	@Inject
	private KTransactionManager transactionManager;
	private SampleDataBase dataBase;

	private static String createNewData() {
		count++;
		return "data - [" + count + "]" + String.valueOf(System.currentTimeMillis());
	}

	@Override
	protected void doSetUp() {
		dataBase = new SampleDataBase();
	}

	@Override
	protected void doTearDown() {
		Assert.assertFalse(transactionManager.hasCurrentTransaction());
		//	On vérifie que la transaction est bien terminée
	}

	//	/**
	//	 * test la description du manager.
	//	 * @throws Exception  si erreur
	//	 */
	//	@Test
	//	public void testDescription() throws Exception {
	//		TestUtil.testDescription(transactionManager);
	//	}

	/**
	 * Il n'est pas possible de créer une transaction courante si celle-ci existe déjà.
	 */
	@Test
	public void testFailCreateCurrentTransaction() {
		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			transactionManager.createCurrentTransaction();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("current transaction already created", e.getMessage());
		}
	}

	/**
	 * Test récupération de la transaction courante.
	 */
	@Test
	public void testGetCurrentTransaction() {
		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			Assert.assertEquals(currentTransaction, transactionManager.getCurrentTransaction());
			currentTransaction.rollback();
		}
	}

	/**
	 * Verifier la gestion du commit.
	 */
	@Test
	public void testCommit() {
		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConection connection = obtainDataBaseConnection(dataBase, "test-memory-1");

			// --- modification de la bdd
			final String value = createNewData();
			connection.setData(value);
			Assert.assertEquals(value, connection.getData());
			currentTransaction.commit();
			//On vérifie que la bdd est mise à jour.
			Assert.assertEquals(value, dataBase.getData());
		}
	}

	/**
	 * Verifier la gestion du rollback.
	 */
	@Test
	public void testRollback() {
		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConection connection = obtainDataBaseConnection(dataBase, "test-memory-1");

			// --- modification de la bdd
			final String oldValue = dataBase.getData();
			final String value = createNewData();
			connection.setData(value);
			Assert.assertEquals(value, connection.getData());
			currentTransaction.rollback();
			Assert.assertEquals(oldValue, dataBase.getData());
		}
	}

	/**
	 * Verifier la gestion du commit.
	 * Impossibilité de commiter deux fois.
	 */
	@Test(expected = Exception.class)
	public void testCommitCommit() {
		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			currentTransaction.commit();

			//L'appel au second commit doit déclencher une exception
			currentTransaction.commit();
		}
	}

	/**
	 * Verifier la gestion du commit.
	 * Impossibilité de commiter après un rollback.
	 */
	@Test(expected = Exception.class)
	public void testRollbackCommit() {
		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			currentTransaction.rollback();

			//L'appel au commit après un rollback doit déclencher une exception
			currentTransaction.commit();
		}
	}

	/**
	 * Verifier la gestion du rollback.
	 * Autorisation de rollbacker après un commit.
	 */
	@Test
	public void testCommitRollback() {
		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			currentTransaction.commit();
			currentTransaction.rollback();
		}
	}

	/**
	 * Verifier la gestion du rollback.
	 * Autorisation de rollbacker après un rollback.
	 */
	@Test
	public void testRollbackRollback() {
		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {
			currentTransaction.rollback();
			currentTransaction.rollback();
		}
	}

	/**
	 * Création d'une transaction automome à l'intérieur d'une transaction.
	 */
	@Test
	public void testCreateAutonomousTransaction() {
		try (final KTransactionWritable rootTransaction = transactionManager.createCurrentTransaction()) {
			final SampleDataBaseConection rootConnection = obtainDataBaseConnection(dataBase, "test-memory-1");
			// --- modification de la bdd sur la transaction principale.
			final String rootValue = createNewData();
			rootConnection.setData(rootValue);
			Assert.assertEquals(rootValue, rootConnection.getData());

			try (final KTransactionWritable autonomousTransaction = transactionManager.createAutonomousTransaction()) {
				final SampleDataBaseConection connection = obtainDataBaseConnection(dataBase, "test-memory-2");
				// --- modification de la bdd sur la transaction autonome.
				final String value = createNewData();
				connection.setData(value);
				Assert.assertEquals(value, connection.getData());
				autonomousTransaction.commit();
				//On vérifie que la bdd est mise à jour.
				Assert.assertEquals(value, dataBase.getData());
				rootTransaction.commit();
				//On vérifie que la bdd est mise à jour.
				Assert.assertEquals(rootValue, dataBase.getData());

				Assert.assertNotSame(rootTransaction, autonomousTransaction);
			}
		}
	}

	/**
	 * Création d'une transaction automome à l'intérieur d'une transaction.
	 * Quand la transaction n'existe pas.
	 */
	@Test(expected = NullPointerException.class)
	public void testFailCreateAutonomousTransaction() {
		try (final KTransactionWritable autonomousTransaction = transactionManager.createAutonomousTransaction()) {
			nop(autonomousTransaction);
		}
	}

	//Utilitaire
	private SampleDataBaseConection obtainDataBaseConnection(final SampleDataBase myDataBase, final String resourceId) {
		// --- resource 1
		final KTransactionResourceId<SampleTransactionResource> transactionResourceId = new KTransactionResourceId<>(KTransactionResourceId.Priority.TOP, resourceId);

		final SampleTransactionResource transactionResourceMock = new SampleTransactionResource(myDataBase);
		transactionManager.getCurrentTransaction().addResource(transactionResourceId, transactionResourceMock);
		return transactionResourceMock;
	}

	/**
	 * Vérifier la gestion du commit sur deux ressources différentes.
	 */
	@Test
	public void testTwoResourcesCommit() {
		//On crée une autre BDD.
		final SampleDataBase secondDataBase = new SampleDataBase();

		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConection connection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			final SampleDataBaseConection connection2 = obtainDataBaseConnection(secondDataBase, "test-memory-2");

			// --- modification des deux bdd
			final String value1 = createNewData();
			connection1.setData(value1);
			Assert.assertEquals(value1, connection1.getData());

			final String value2 = createNewData();
			connection2.setData(value2);
			Assert.assertEquals(value2, connection2.getData());

			// --- test du commit
			currentTransaction.commit();
			Assert.assertEquals(value1, dataBase.getData());
			Assert.assertEquals(value2, secondDataBase.getData());
		}
	}

	/**
	 * Vérifier la gestion du rollback sur deux ressources différentes.
	 */
	@Test
	public void testTwoResourcesRollback() {
		//On crée une autre BDD.
		final SampleDataBase secondDataBase = new SampleDataBase();

		try (final KTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConection connection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			final SampleDataBaseConection connection2 = obtainDataBaseConnection(secondDataBase, "test-memory-2");

			final String oldValue1 = dataBase.getData();
			final String oldValue2 = secondDataBase.getData();

			// --- modification des deux bdd
			final String value1 = createNewData();
			connection1.setData(value1);
			Assert.assertEquals(value1, connection1.getData());

			final String value2 = createNewData();
			connection2.setData(value2);
			Assert.assertEquals(value2, connection2.getData());

			currentTransaction.rollback();
			// --- test du rollback
			Assert.assertEquals(oldValue1, dataBase.getData());
			Assert.assertEquals(oldValue2, secondDataBase.getData());
		}
	}

	/**
	 * Test @Transactional aspect placed upon all methods of BusinessServices component.
	 */
	@Test
	public void testTransactional() {
		final String value = businessServices.test();
		businessServices.check(value);
	}
}
