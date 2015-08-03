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
package io.vertigo.dynamo.work;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.work.mock.DivideWork;
import io.vertigo.dynamo.work.mock.DivideWorkEngine;
import io.vertigo.dynamo.work.mock.SlowWork;
import io.vertigo.dynamo.work.mock.SlowWorkEngine;
import io.vertigo.dynamo.work.mock.ThreadLocalWork;
import io.vertigo.dynamo.work.mock.ThreadLocalWorkEngine;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public abstract class AbstractWorkManagerTest extends AbstractTestCaseJU4 {
	private final long warmupTime = 5000; //en fonction du mode de distribution la prise en compte d'une tache est plus ou moins longue. Pour les TU on estime à 2s
	private static final int WORKER_COUNT = 5; //Doit correspondre au workerCount déclaré dans managers.xlm

	@Inject
	private WorkManager workManager;

	//=========================================================================
	//===========================PROCESS=======================================
	//=========================================================================
	@Test
	public void testProcess() {
		final DivideWork work = new DivideWork(10, 5);
		final long div = workManager.process(work, new WorkEngineProvider<>(DivideWorkEngine.class));
		Assert.assertEquals(10L / 5L, div);
	}

	@Test
	public void testProcessor() {
		//	final DivideWork work = new DivideWork(10, 5);
		final long result = workManager
				.createProcessor(new WorkEngineProvider<>(LengthWorkEngine.class))
				.then(SquareWorkEngine.class)
				.then(SquareWorkEngine.class)
				.exec("aa");
		Assert.assertEquals(2 * 2 * 2 * 2L, result);
	}

	public static final class LengthWorkEngine implements WorkEngine<Long, String> {
		/** {@inheritDoc} */
		@Override
		public Long process(final String work) {
			return work.length() * 1L;
		}
	}

	public static final class SquareWorkEngine implements WorkEngine<Long, Long> {
		/** {@inheritDoc} */
		@Override
		public Long process(final Long work) {
			return work.longValue() * work.longValue();
		}
	}

	//	public final class SquareWorkEngine implements WorkEngine<Long, Long> {
	//		/** {@inheritDoc} */
	//		public Long process(final Long work) {
	//			return work.longValue()*work.longValue();
	//		}
	//	}

	@Test(expected = NullPointerException.class)
	public void testProcessWithNull() {
		final DivideWork work = null;
		final long div = workManager.process(work, new WorkEngineProvider<>(DivideWorkEngine.class));
		nop(div);
	}

	@Test(expected = ArithmeticException.class)
	public void testProcessWithError() {
		final DivideWork work = new DivideWork(10, 0);
		final long div = workManager.process(work, new WorkEngineProvider<>(DivideWorkEngine.class));
		nop(div);
	}

	//=========================================================================
	//===========================SCHEDULE======================================
	//=========================================================================

	/**
	 * test of 2 async executions
	 */
	@Test
	public void testSchedule() throws InterruptedException {
		final DivideWork work = new DivideWork(10, 5);
		final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
		workManager.schedule(work, new WorkEngineProvider<>(DivideWorkEngine.class), workResultHanlder);
		workManager.schedule(work, new WorkEngineProvider<>(DivideWorkEngine.class), workResultHanlder);
		Thread.sleep(1000);
		//---
		final boolean finished = workResultHanlder.waitFinish(2, warmupTime);
		if (!finished) {
			System.err.println("Not finished (" + workResultHanlder.toString());
		}
		Assert.assertTrue(finished);
		Assert.assertEquals(2, workResultHanlder.getLastResult().intValue());
		Assert.assertEquals(null, workResultHanlder.getLastThrowable());
	}

	@Test(expected = NullPointerException.class)
	public void testScheduleWithNull() {
		final DivideWork work = null;
		final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
		workManager.schedule(work, new WorkEngineProvider<>(DivideWorkEngine.class), workResultHanlder);
	}

	/**
	 * Teste l'exécution asynchrone d'une tache avec erreur. (Division par 0)
	 */
	@Test
	public void testScheduleError() {
		final DivideWork work = new DivideWork(10, 0);
		final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
		workManager.schedule(work, new WorkEngineProvider<>(DivideWorkEngine.class), workResultHanlder);

		final boolean finished = workResultHanlder.waitFinish(1, warmupTime);
		//On vérifie plusieurs  choses
		// -que l'erreur remontée est bien une ArithmeticException
		//- que l'exception est contenue dans le handler
		if (!finished) {
			System.err.println("Not finished (" + workResultHanlder.toString());
		}
		Assert.assertTrue(finished);
		Assert.assertEquals(null, workResultHanlder.getLastResult());
		Assert.assertEquals(ArithmeticException.class, workResultHanlder.getLastThrowable().getClass());
	}

	/**
	 * Teste l'exécution asynchrone d'une tache avec une durée de timeOut trop courte.
	 */
	@Test
	public void testScheduleWithTimeOut() {
		final int workTime = 5 * 1000; //5s temps d'exécution d'un work
		final MyWorkResultHanlder<Boolean> workResultHanlder = new MyWorkResultHanlder<>();
		final SlowWork work = new SlowWork(workTime);
		workManager.schedule(work, new WorkEngineProvider<>(SlowWorkEngine.class), workResultHanlder);
		final boolean finished = workResultHanlder.waitFinish(1, workTime - 1000);
		//-----
		//We are expecting a time out if we are waiting less than execution's time.
		Assert.assertFalse(finished);
	}

	//=========================================================================
	//=========================================================================
	//=========================================================================

	/**
	 * Teste l'exécution asynchrone de plusieurs taches.
	 * - On démarre 10 (2 fois le nombre de workerCount) Travaux qui ne font rien qu'attendre 5s.
	 * On vérifie que si on attend 10s + marge alors toutes les taches sont exécutées.
	 */
	@Test
	public void testWaitForAll() {
		final int workTime = 5 * 1000; //5s temps d'exécution d'un work
		final MyWorkResultHanlder<Boolean> workResultHanlder = new MyWorkResultHanlder<>();

		createWorkItems(WORKER_COUNT * 2, workTime, workResultHanlder);
		final boolean finished = workResultHanlder.waitFinish(WORKER_COUNT * 2, 2 * workTime + warmupTime);

		//On estime que la durée max n'excéde pas le workTime + 1000ms
		//-----
		Assert.assertTrue(finished);

	}

	/**
	 * Teste l'exécution asynchrone de très nombreuses taches.
	 * - On démarre 50 (10 fois le nombre de workerCount) Travaux qui ne font rien qu'attendre 2s.
	 * On vérifie que les taches sont toutes prise en charge avant la fin.
	 * On vérifie que si on attend 10*2s + marge alors toutes les taches sont exécutées.
	 */
	@Test
	public void testWaitForAllMassWork() {
		final int workToCreate = 10 * WORKER_COUNT;
		final int workTime = 2 * 1000; //2s temps d'exécution d'un work
		final long start = System.currentTimeMillis();
		final MyWorkResultHanlder<Boolean> workResultHanlder = new MyWorkResultHanlder<>();
		createWorkItems(workToCreate, workTime, workResultHanlder);
		final long timeout = 10 * workTime + warmupTime;
		Assert.assertTrue("Shedule de " + workToCreate + " work trop long : " + (System.currentTimeMillis() - start) + "ms", System.currentTimeMillis() - start < 100);

		final boolean finished = workResultHanlder.waitFinish(workToCreate, timeout);
		//On estime que la durée max n'excéde pas le workTime + 1000ms
		//-----
		Assert.assertTrue("Les works n'ont pas terminés dans les temps, le timeout à " + timeout + "ms s'est déclenché", finished);

	}

	/**
	 * Teste les Work qui vident correctement leur threadLocal.
	 */
	@Test
	public void testThreadLocalWorkReset() {
		final int workToCreate = 20 * WORKER_COUNT;
		final int workTime = 200; //200ms temps d'exécution d'un work
		final MyWorkResultHanlder<Integer> workResultHanlder = new MyWorkResultHanlder<>();
		createThreadLocalWorkItems(workToCreate, workTime, true, workResultHanlder);

		final boolean finished = workResultHanlder.waitFinish(workToCreate, workTime * workToCreate);
		//-----
		Assert.assertTrue("Les works n'ont pas terminés dans les temps, le timeout à " + workTime * workToCreate + "ms s'est déclenché", finished);
		Assert.assertEquals("ThreadLocal conservé entre deux exécutions ", Integer.valueOf(1), workResultHanlder.getLastResult());
	}

	/**
	 * Teste les Work qui NE vident PAS leur threadLocal.
	 */
	@Test
	public void testThreadLocalWorkNotReset() {
		final int workToCreate = 20 * WORKER_COUNT;
		final int workTime = 200; //200ms temps d'exécution d'un work
		final MyWorkResultHanlder<Integer> workResultHanlder = new MyWorkResultHanlder<>();
		createThreadLocalWorkItems(workToCreate, workTime, false, workResultHanlder);

		final boolean finished = workResultHanlder.waitFinish(workToCreate, 50 * workTime * workToCreate);
		//-----
		Assert.assertTrue("Les works n'ont pas terminés dans les temps, le timeout à " + workTime * workToCreate + "ms s'est déclanché", finished);
		Assert.assertEquals("ThreadLocal conservé entre deux exécutions ", Integer.valueOf(1), workResultHanlder.getLastResult());
	}

	private void createWorkItems(final int workToCreate, final int workTime, final WorkResultHandler<Boolean> workResultHanlder) {
		for (int i = 0; i < workToCreate; i++) {
			workManager.schedule(new SlowWork(workTime), new WorkEngineProvider<>(SlowWorkEngine.class), workResultHanlder);
		}
	}

	private void createThreadLocalWorkItems(final int workToCreate, final int workTime, final boolean clearThreadLocal, final WorkResultHandler<Integer> workResultHanlder) {
		for (int i = 0; i < workToCreate; i++) {
			workManager.schedule(new ThreadLocalWork(workTime, clearThreadLocal), new WorkEngineProvider<>(ThreadLocalWorkEngine.class), workResultHanlder);
		}
	}
}
