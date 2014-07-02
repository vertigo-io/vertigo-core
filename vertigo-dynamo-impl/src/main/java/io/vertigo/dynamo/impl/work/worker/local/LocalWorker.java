package io.vertigo.dynamo.impl.work.worker.local;

import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation d'un pool local de {@link Worker}.
 * 
 * @author pchretien
 */
public final class LocalWorker implements Worker, Activeable {
	/** paramètre du plugin définissant la taille de la queue. */
	private final WorkersPool workersPool;
	private boolean active;

	/**
	 * Constructeur.
	 * 
	 * @param workerCount paramètres d'initialisation du pool
	 */
	public LocalWorker(final int workerCount) {
		Assertion.checkArgument(workerCount >= 1, "At least one thread must be allowed to process asynchronous jobs.");
		// ---------------------------------------------------------------------
		workersPool = new WorkersPool(this, workerCount);
	}

	/** {@inheritDoc} */
	public void start() {
		workersPool.start();
		active = true;
	}

	/** {@inheritDoc} */
	public void stop() {
		active = false;
		workersPool.stop();
	}

	/** {@inheritDoc} */
	public <WR, W> void schedule(final WorkItem<WR, W> workItem) {
		Assertion.checkArgument(active, "plugin is not yet started");
		Assertion.checkNotNull(workItem);
		// ---------------------------------------------------------------------
		workersPool.putWorkItem(workItem);
	}

	/** {@inheritDoc} */
	public <WR, W> void process(final WorkItem<WR, W> workItem) {
		Assertion.checkArgument(active, "plugin is not yet started");
		Assertion.checkNotNull(workItem);
		// ---------------------------------------------------------------------
		workItem.setResult(workItem.getWorkEngineProvider().provide().process(workItem.getWork()));
	}
}
