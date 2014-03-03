package io.vertigo.dynamo.impl.work;

import io.vertigo.dynamo.impl.work.listener.WorkListener;
import io.vertigo.dynamo.impl.work.listener.WorkListenerImpl;
import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.impl.work.worker.local.WorkItem;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation de workManager.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: WorkManagerImpl.java,v 1.7 2014/01/20 11:34:32 npiedeloup Exp $
 */
public final class WorkManagerImpl implements WorkManager, Activeable {

	private static final Object DUMMY_WORK = new Object();

	private final LocalWorker localWorker;

	@Inject
	private Option<DistributedWorkerPlugin> distributedWorker;
	private final WorkListener workListener;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager de la performance applicative
	 */
	@Inject
	public WorkManagerImpl(final @Named("workerCount") int workerCount/*, final AnalyticsManager analyticsManager*/) {
		workListener = new WorkListenerImpl(/*analyticsManager*/);
		localWorker = new LocalWorker(workerCount);
	}

	/** {@inheritDoc} */
	public void start() {
		//localWorker n'étant pas un plugin 
		//il faut le démarrer et l'arréter explicitement.
		localWorker.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		localWorker.stop();
	}

	/** {@inheritDoc}   */
	public <WR, W> WR process(final W work, final WorkEngineProvider<WR, W> workEngineProvider) {
		Assertion.checkNotNull(work);
		//----------------------------------------------------------------------
		//On délégue l'exécution synchrone et locale à un Worker.
		workListener.onStart(workEngineProvider.getName());
		boolean executed = false;
		final long start = System.currentTimeMillis();
		try {
			final WR workResult = resolveWorker(workEngineProvider).process(work, workEngineProvider);
			executed = true;
			return workResult;
		} finally {
			workListener.onFinish(workEngineProvider.getName(), System.currentTimeMillis() - start, executed);
		}
	}

	private <WR, W> Worker resolveWorker(final WorkEngineProvider<WR, W> workEngineProvider) {
		/* 
		 * On recherche un Worker capable d'effectuer le travail demandé.
		 * 1- On recherche parmi les works externes 
		 * 2- Si le travail n'est pas déclaré comme étant distribué on l'exécute localement
		 */
		if (distributedWorker.isDefined() && distributedWorker.get().canProcess(workEngineProvider)) {
			return distributedWorker.get();
		}
		return localWorker;
		//Gestion de la stratégie de distribution des works
		//		return distributedWorkerPlugin.isDefined() && distributedWorkerPlugin.get().getWorkFamily().equalsIgnoreCase(work.getWorkFamily());
	}

	/** {@inheritDoc} */
	public <WR, W> void schedule(final W work, final WorkEngineProvider<WR, W> workEngineProvider, final WorkResultHandler<WR> workResultHandler) {
		Assertion.checkNotNull(work);
		//----------------------------------------------------------------------
		final WorkItem<WR, W> workItem = new WorkItem<>(work, workEngineProvider, workResultHandler);
		resolveWorker(workEngineProvider).schedule(workItem);
	}

	/** {@inheritDoc} */
	public <WR, W> void async(final Callable<WR> callable, final WorkResultHandler<WR> workResultHandler) {
		schedule(DUMMY_WORK, new WorkEngineProvider<>(new AsyncEngine<>(callable)), workResultHandler);
	}
}
