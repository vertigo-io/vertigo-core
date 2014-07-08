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
	private final long warmupTime = 2000; //en fonction du mode de distribution la prise en compte d'une tache est plus ou moins longue. Pour les TU on estime à 2s
	private final int loop = 100;
	private static final int WORKER_COUNT = 5; //Doit correspondre au workerCount déclaré dans managers.xlm

	@Inject
	private WorkManager workManager;

	/**
	 * Teste l'exécution synchrone d'une tache.(Division)
	 * On effectue l'opération n fois.
	 */
	@Test
	public void testProcess() {
		final long start = System.currentTimeMillis();
		for (int i = 0; i < loop; i++) {
			final WorkItem<Long, DivideWork> workItem = new WorkItem<>(new DivideWork(10, 5), new WorkEngineProvider<>(DivideWorkEngine.class));
			workManager.process(workItem);
			final long div = workItem.getResult();
			Assert.assertEquals(2L, div);
			if (i > 0 && i % 1000 == 0) {
				final long elapsed = System.currentTimeMillis() - start;
				System.out.println(">>processed : " + i + " in " + 1000 * elapsed / i + " ms/1000exec");
			}
		}
		final long elapsed = System.currentTimeMillis() - start;
		System.out.println("Process " + loop + ">>>>>> in " + elapsed + " ms");
	}

	/**
	 * Teste l'exécution synchrone d'une tache.(Division)
	 */
	@Test(expected = NullPointerException.class)
	public void testProcessWithNull() {
		final DivideWork work = null;
		final WorkItem<Long, DivideWork> workItem = new WorkItem<>(work, new WorkEngineProvider<>(DivideWorkEngine.class));
		workManager.process(workItem);
		final Object div = workItem.getResult();
		nop(div);
	}

	/**
	 * Teste l'exécution synchrone d'une tache avec erreur. (Division par 0)
	 */
	@Test(expected = ArithmeticException.class)
	public void testProcessWithError() {
		final WorkItem<Long, DivideWork> workItem = new WorkItem<>(new DivideWork(10, 0), new WorkEngineProvider<>(DivideWorkEngine.class));
		workManager.process(workItem);
		final long div = workItem.getResult();
		nop(div);
	}

	/**
	 * Teste l'exécution asynchrone d'une tache.
	 */
	@Test
	public void testSchedule() {
		final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
		final long start = System.currentTimeMillis();
		for (int i = 0; i < loop; i++) {
			final WorkItem<Long, DivideWork> workItem = new WorkItem<>(new DivideWork(10, 5), new WorkEngineProvider<>(DivideWorkEngine.class), workResultHanlder);
			workManager.schedule(workItem);
			if (i > 0 && i % 1000 == 0) {
				final long elapsed = System.currentTimeMillis() - start;
				System.out.println(">sending>" + i + " in " + 1000 * elapsed / i + " ms/1000exec");
			}
		}
		final boolean finished = workResultHanlder.waitFinish(loop, 20 * 1000); //20s de timeout, le test prend normalement 2s
		Assert.assertEquals("résultat : " + workResultHanlder, true, finished);
		Assert.assertEquals(2, workResultHanlder.getLastResult().intValue());

		//		final long elapsed = System.currentTimeMillis() - start;
		//		System.out.println("testSchedule>>>>>> in " + elapsed + " ms");
		//		System.out.println(workResultHanlder);
	}

	/**
	 * Teste l'exécution asynchrone d'une tache.
	 */
	@Test(expected = NullPointerException.class)
	public void testScheduleWithNull() {
		final DivideWork work = null;
		final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
		//ON va déclencher une assertion
		final WorkItem<Long, DivideWork> workItem = new WorkItem<>(work, new WorkEngineProvider<>(DivideWorkEngine.class), workResultHanlder);
		workManager.schedule(workItem);
	}

	/**
	 * Teste l'exécution asynchrone d'une tache avec erreur. (Division par 0)
	 */
	@Test
	public void testScheduleError() {
		final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
		final WorkItem<Long, DivideWork> workItem = new WorkItem<>(new DivideWork(10, 0), new WorkEngineProvider<>(DivideWorkEngine.class), workResultHanlder);
		workManager.schedule(workItem);

		final boolean finished = workResultHanlder.waitFinish(1, warmupTime);
		//On vérifie plusieurs  choses 
		// -que l'erreur remontée est bien une ArithmeticException
		//- que l'exception est contenue dans le handler
		Assert.assertEquals(null, workResultHanlder.getLastResult());
		Assert.assertTrue(workResultHanlder.getLastThrowable() instanceof ArithmeticException);
		Assert.assertEquals(true, finished);
	}

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
		//---------------------------------------------------------------------
		Assert.assertEquals(true, finished);

	}

	/**
	 * Teste l'exécution asynchrone d'une tache avec une durée de timeOut trop courte.
	 */
	@Test
	public void testWaitForAllWithTimeOut() {
		final int workTime = 5 * 1000; //5s temps d'exécution d'un work
		final MyWorkResultHanlder<Boolean> workResultHanlder = new MyWorkResultHanlder<>();
		final long time = System.currentTimeMillis();
		createWorkItems(WORKER_COUNT * 2, workTime, workResultHanlder);
		final boolean finished = workResultHanlder.waitFinish(WORKER_COUNT * 2, 2 * workTime - 1000);
		System.out.println("finished:" + finished + " in " + (System.currentTimeMillis() - time) + " ms");
		//On estime que la durée max n'excéde pas le workTime + 500ms
		//---------------------------------------------------------------------
		Assert.assertEquals("résultat : " + workResultHanlder, false, finished);
		//		for (final WorkItem<Boolean> workItem : workItems) {
		//			assertTrue("WorkItem " + workItems.indexOf(workItem) + " non terminé ", workItem.getStatus() == Status.Completed);
		//		}
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
		//---------------------------------------------------------------------
		Assert.assertEquals("Les works n'ont pas terminés dans les temps, le timeout à " + timeout + "ms s'est déclenché", true, finished);

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
		//---------------------------------------------------------------------
		Assert.assertEquals("Les works n'ont pas terminés dans les temps, le timeout à " + workTime * workToCreate + "ms s'est déclenché", true, finished);
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
		//---------------------------------------------------------------------
		Assert.assertEquals("Les works n'ont pas terminés dans les temps, le timeout à " + workTime * workToCreate + "ms s'est déclanché", true, finished);
		Assert.assertEquals("ThreadLocal conservé entre deux exécutions ", Integer.valueOf(1), workResultHanlder.getLastResult());
	}

	private void createWorkItems(final int workToCreate, final int workTime, final WorkResultHandler<Boolean> workResultHanlder) {
		for (int i = 0; i < workToCreate; i++) {
			final WorkItem<Boolean, SlowWork> workItem = new WorkItem<>(new SlowWork(workTime), new WorkEngineProvider<>(SlowWorkEngine.class), workResultHanlder);
			workManager.schedule(workItem);
		}
	}

	private void createThreadLocalWorkItems(final int workToCreate, final int workTime, final boolean clearThreadLocal, final WorkResultHandler<Integer> workResultHanlder) {
		for (int i = 0; i < workToCreate; i++) {
			final WorkItem<Integer, ThreadLocalWork> workItem = new WorkItem<>(new ThreadLocalWork(workTime, clearThreadLocal), new WorkEngineProvider<>(ThreadLocalWorkEngine.class), workResultHanlder);
			workManager.schedule(workItem);
		}
	}
}
