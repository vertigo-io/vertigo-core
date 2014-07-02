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
		Assertion.checkArgument(workerCount >= 1, "Il faut définir au moins un thread pour gérer les traitements asynchrones.");
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
		Assertion.checkArgument(active, "le plugin n'est pas dans un état démarré");
		Assertion.checkNotNull(workItem);
		// ---------------------------------------------------------------------
		workersPool.putWorkItem(workItem);
	}

	/** {@inheritDoc} */
	public <WR, W> void process(final WorkItem<WR, W> workItem) {
		Assertion.checkArgument(active, "le plugin n'est pas dans un état démarré");
		Assertion.checkNotNull(workItem);
		// ---------------------------------------------------------------------
		workItem.setResult(workItem.getWorkEngineProvider().provide().process(workItem.getWork()));
	}
}
