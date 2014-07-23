package io.vertigo.dynamo.impl.work;

import io.vertigo.dynamo.impl.work.listener.WorkListener;
import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

final class WCoordinatorImpl implements WCoordinator {
	private final WorkListener workListener;
	private final Option<DistributedWorkerPlugin> distributedWorker;
	private final LocalWorker localWorker;

	WCoordinatorImpl(int workerCount, WorkListener workListener, Option<DistributedWorkerPlugin> distributedWorker) {
		Assertion.checkNotNull(workListener);
		Assertion.checkNotNull(distributedWorker);
		//-----------------------------------------------------------------
		localWorker = new LocalWorker(workerCount);
		this.workListener = workListener;
		this.distributedWorker = distributedWorker;
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
	public <WR, W> void process(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//----------------------------------------------------------------------
		//On délégue l'exécution synchrone et locale à un Worker.
		workListener.onStart(workItem.getWorkEngineProvider().getName());
		boolean executed = false;
		final long start = System.currentTimeMillis();
		try {
			resolveWorker(workItem).process(workItem);
			executed = true;
		} finally {
			workListener.onFinish(workItem.getWorkEngineProvider().getName(), System.currentTimeMillis() - start, executed);
		}
	}

	private <WR, W> Worker resolveWorker(final WorkItem<WR, W> workItem) {
		/* 
		 * On recherche un Worker capable d'effectuer le travail demandé.
		 * 1- On recherche parmi les works externes 
		 * 2- Si le travail n'est pas déclaré comme étant distribué on l'exécute localement
		 */
		if (distributedWorker.isDefined() && distributedWorker.get().canProcess(workItem.getWorkEngineProvider())) {
			return distributedWorker.get();
		}
		return localWorker;
		//Gestion de la stratégie de distribution des works
		//		return distributedWorkerPlugin.isDefined() && distributedWorkerPlugin.get().getWorkFamily().equalsIgnoreCase(work.getWorkFamily());
	}

	/** {@inheritDoc} */
	public <WR, W> void schedule(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//----------------------------------------------------------------------
		resolveWorker(workItem).schedule(workItem);
	}
}
