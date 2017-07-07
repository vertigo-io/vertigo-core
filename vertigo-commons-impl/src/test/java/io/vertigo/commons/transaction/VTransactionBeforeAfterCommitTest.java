/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.transaction;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.transaction.data.SampleDataBase;
import io.vertigo.commons.transaction.data.SampleDataBaseConnection;
import io.vertigo.commons.transaction.data.SampleTransactionResource;

/**
 * @author npiedeloup
 */
public final class VTransactionBeforeAfterCommitTest extends AbstractTestCaseJU4 {

	private final AtomicBoolean run1BeforeCommit = new AtomicBoolean(false);
	private final AtomicBoolean run2BeforeCommit = new AtomicBoolean(false);
	private final AtomicBoolean run3BeforeCommit = new AtomicBoolean(false);
	private final AtomicBoolean run1AfterCommit = new AtomicBoolean(false);
	private final AtomicBoolean run2AfterCommit = new AtomicBoolean(false);
	private final AtomicBoolean run3AfterCommit = new AtomicBoolean(false);

	@Inject
	private VTransactionManager transactionManager;
	private SampleDataBase dataBase;

	@Override
	protected void doSetUp() {
		dataBase = new SampleDataBase();
	}

	//Utilitaire
	private SampleDataBaseConnection obtainDataBaseConnection(final SampleDataBase myDataBase, final String resourceId) {
		// --- resource 1
		final VTransactionResourceId<SampleTransactionResource> transactionResourceId = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, resourceId);

		final SampleTransactionResource sampleTransactionResource = new SampleTransactionResource(myDataBase);
		transactionManager.getCurrentTransaction().addResource(transactionResourceId, sampleTransactionResource);
		return sampleTransactionResource;
	}

	/**
	 * Test beforeCommit and afterCommit functions.
	 */
	@Test
	public void testSimplerCase() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection sampleDataBaseConnection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			sampleDataBaseConnection1.setData("TEST-SYNCHRONIZATION-COMMIT");

			final boolean beforeCommitError = false;
			final boolean afterCommitError = false;
			registerBeforeAfterCommit(currentTransaction, new ErronousTransactionResource(null, null, null), beforeCommitError, afterCommitError);

			Assert.assertNull(dataBase.getData());

			currentTransaction.commit();
		} finally {
			//resource was committed
			Assert.assertEquals("TEST-SYNCHRONIZATION-COMMIT", dataBase.getData());

			//all beforeCommit was proceeded
			Assert.assertTrue(run1BeforeCommit.get());
			Assert.assertTrue(run2BeforeCommit.get());
			Assert.assertTrue(run3BeforeCommit.get());

			//all afterCommit was proceeded
			Assert.assertTrue(run1AfterCommit.get());
			Assert.assertTrue(run2AfterCommit.get());
			Assert.assertTrue(run3AfterCommit.get());
		}
	}

	/**
	 * Test error while proceeding beforeCommit.
	 */
	@Test(expected = ArithmeticException.class)
	public void testBeforeCommitErrors() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection sampleDataBaseConnection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			sampleDataBaseConnection1.setData("TEST-SYNCHRONIZATION-COMMIT");

			final boolean beforeCommitError = true;
			final boolean afterCommitError = false;
			registerBeforeAfterCommit(currentTransaction, new ErronousTransactionResource(null, null, null), beforeCommitError, afterCommitError);

			Assert.assertNull(dataBase.getData());

			currentTransaction.commit();
		} finally {
			//not committed
			Assert.assertNull(dataBase.getData());

			//only beforeCommit before error was proceeded
			Assert.assertTrue(run1BeforeCommit.get());
			Assert.assertTrue(run2BeforeCommit.get());
			Assert.assertFalse(run3BeforeCommit.get());

			//none afterCommit was proceeded
			Assert.assertFalse(run1AfterCommit.get());
			Assert.assertFalse(run2AfterCommit.get());
			Assert.assertFalse(run3AfterCommit.get());
		}
	}

	/**
	 * Test error while proceeding afterCommit.
	 */
	@Test
	public void testAfterCommitErrors() {

		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection sampleDataBaseConnection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			sampleDataBaseConnection1.setData("TEST-SYNCHRONIZATION-COMMIT");

			final boolean beforeCommitError = false;
			final boolean afterCommitError = false;
			registerBeforeAfterCommit(currentTransaction, new ErronousTransactionResource(null, null, null), beforeCommitError, afterCommitError);

			Assert.assertNull(dataBase.getData());

			currentTransaction.commit();

			//No exception expected
		} finally {
			Assert.assertEquals("TEST-SYNCHRONIZATION-COMMIT", dataBase.getData()); //resource was committed

			//all beforeCommit was proceeded
			Assert.assertTrue(run1BeforeCommit.get());
			Assert.assertTrue(run2BeforeCommit.get());
			Assert.assertTrue(run3BeforeCommit.get());
			//all afterCommit was proceeded
			Assert.assertTrue(run1AfterCommit.get());
			Assert.assertTrue(run2AfterCommit.get());
			Assert.assertTrue(run3AfterCommit.get());
		}
	}

	/**
	 * Test errors while proceeding beforeCommit and afterCommit.
	 */
	@Test(expected = ArithmeticException.class)
	public void testBeforeAfterCommitErrors() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection sampleDataBaseConnection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			sampleDataBaseConnection1.setData("TEST-SYNCHRONIZATION-COMMIT");

			final boolean beforeCommitError = true;
			final boolean afterCommitError = true;
			registerBeforeAfterCommit(currentTransaction, new ErronousTransactionResource(null, null, null), beforeCommitError, afterCommitError);

			Assert.assertNull(dataBase.getData());

			currentTransaction.commit();
		} finally {
			//not committed
			Assert.assertNull(dataBase.getData());

			//only beforeCommit before error was proceeded
			Assert.assertTrue(run1BeforeCommit.get());
			Assert.assertTrue(run2BeforeCommit.get());
			Assert.assertFalse(run3BeforeCommit.get());

			//none afterCommit was proceeded
			Assert.assertFalse(run1AfterCommit.get());
			Assert.assertFalse(run2AfterCommit.get());
			Assert.assertFalse(run3AfterCommit.get());
		}
	}

	/**
	 * Test beforeCommit and afterCommit functions.
	 * With error on commit resource.
	 */
	@Test(expected = Error.class)
	public void testBeforeAfterCommitWithErrorOnCommitResource() {
		try (final VTransactionWritable currentTransaction = transactionManager.createCurrentTransaction()) {

			final SampleDataBaseConnection sampleDataBaseConnection1 = obtainDataBaseConnection(dataBase, "test-memory-1");
			sampleDataBaseConnection1.setData("TEST-SYNCHRONIZATION-COMMIT");

			final boolean beforeCommitError = false;
			final boolean afterCommitError = false;
			registerBeforeAfterCommit(currentTransaction, new ErronousTransactionResource(new Error("SpecificException on commit"), null, null), beforeCommitError, afterCommitError);

			Assert.assertNull(dataBase.getData());

			currentTransaction.commit();
		} finally {
			//not committed
			Assert.assertNull(dataBase.getData());

			//all beforeCommit was proceeded
			Assert.assertTrue(run1BeforeCommit.get());
			Assert.assertTrue(run2BeforeCommit.get());
			Assert.assertTrue(run3BeforeCommit.get());

			//none afterCommit was proceeded
			Assert.assertFalse(run1AfterCommit.get());
			Assert.assertFalse(run2AfterCommit.get());
			Assert.assertFalse(run3AfterCommit.get());
		}
		Assert.fail();
	}

	private void registerBeforeAfterCommit(final VTransaction currentTransaction, final ErronousTransactionResource transactionResource, final boolean beforeCommitError, final boolean afterCommitError) {

		final VTransactionResourceId<VTransactionResource> transactionResourceId = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, "Ressource");
		transactionManager.getCurrentTransaction().addResource(transactionResourceId, transactionResource);

		currentTransaction.addBeforeCommit(() -> {
			Assert.assertNull(dataBase.getData());
			run1BeforeCommit.set(true);
		});

		currentTransaction.addBeforeCommit(() -> {
			run2BeforeCommit.set(true);
			if (beforeCommitError) {
				throw new ArithmeticException("Can't proceed this code");
			}
		});

		currentTransaction.addBeforeCommit(() -> run3BeforeCommit.set(true));

		currentTransaction.addAfterCompletion(new VTransactionAfterCompletionFunction() {

			@Override
			public void afterCompletion(final boolean txCommited) {
				run1AfterCommit.set(txCommited);
				Assert.assertEquals("TEST-SYNCHRONIZATION-COMMIT", dataBase.getData());
			}
		});

		currentTransaction.addAfterCompletion(new VTransactionAfterCompletionFunction() {
			@Override
			public void afterCompletion(final boolean txCommited) {
				run2AfterCommit.set(txCommited);
				if (afterCommitError) {
					throw new ArithmeticException("Can't proceed this code");
				}
			}
		});

		currentTransaction.addAfterCompletion(new VTransactionAfterCompletionFunction() {
			@Override
			public void afterCompletion(final boolean txCommited) {
				run3AfterCommit.set(txCommited);
			}
		});
	}

	private static class ErronousTransactionResource implements VTransactionResource {

		Throwable throwOnCommit = null;
		Throwable throwOnRollback = null;
		Throwable throwOnRelease = null;

		ErronousTransactionResource(final Throwable throwOnCommit, final Throwable throwOnRollback, final Throwable throwOnRelease) {
			this.throwOnCommit = throwOnCommit;
			this.throwOnRollback = throwOnRollback;
			this.throwOnRelease = throwOnRelease;
		}

		@Override
		public void commit() throws Exception {
			if (throwOnCommit != null) {
				doThrow(throwOnCommit);
			}
		}

		@Override
		public void rollback() throws Exception {
			if (throwOnRollback != null) {
				doThrow(throwOnRollback);
			}
		}

		@Override
		public void release() throws Exception {
			if (throwOnRelease != null) {
				doThrow(throwOnRelease);
			}
		}

		private static void doThrow(final Throwable t) throws Exception {
			if (t instanceof Exception) {
				throw (Exception) t;
			}
			throw (Error) t;
		}
	}
}
