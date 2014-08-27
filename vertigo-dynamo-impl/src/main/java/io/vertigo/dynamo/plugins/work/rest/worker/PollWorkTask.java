package io.vertigo.dynamo.plugins.work.rest.worker;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

/**
 * Tache runnable permettant l'exécution d'un travail.
 * @author npiedeloup
 */
final class PollWorkTask<WR> implements Runnable {
	//A unifier avec RedisDispatcher
	//A unifier avec RedisDispatcher
	//A unifier avec RedisDispatcher
	//A unifier avec RedisDispatcher
	private final WorkQueueRestClient workQueueClient;
	private final String workType;
	private final LocalWorker localWorker;
	/**
	 * Constructeur.
	 * @param workType Type de work
	 * @param workQueueClient Client REST
	 */
	PollWorkTask(final String workType, final WorkQueueRestClient workQueueClient,  final LocalWorker localWorker) {
		Assertion.checkArgNotEmpty(workType);
		Assertion.checkNotNull(workQueueClient);
		Assertion.checkNotNull(localWorker);
		//---------------------------------------------------------------------
		this.workType = workType;
		this.workQueueClient = workQueueClient;
		this.localWorker = localWorker;
	}

	/** {@inheritDoc} */
	public void run() {
		while (!Thread.interrupted()) {
			doRun();
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	private <WR, W> void doRun() {
		final WorkItem workItem = workQueueClient.pollWorkItem(workType);
		if (workItem != null) {
			final Option<WorkResultHandler<WR>> workResultHandler = Option.<WorkResultHandler<WR>> some(new WorkResultHandler<WR>() {
				public void onStart() {
					workQueueClient.sendOnStart(workItem.getId());
				}

				public void onDone(final boolean succeeded, final WR result, final Throwable error) {
					workQueueClient.sendOnDone(workItem.getId(), succeeded, result, error);
				}
			});
			localWorker.submit(workItem, workResultHandler);
			//On rerentre dans le WorkItemExecutor pour traiter le travail
			//Le workResultHandler sait déjà répondre au serveur pour l'avancement du traitement
			//				final WorkItemExecutor workItemExecutor = new WorkItemExecutor(nextWorkItem);
			//				workItemExecutor.run();
		}
	}
}
