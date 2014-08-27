package io.vertigo.dynamo.plugins.work.rest.worker;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.concurrent.Callable;

/**
	 * Tache runnable permettant l'exécution d'un travail.
	 * @author npiedeloup
	 */
final class PollWorkTask<WR> implements Callable<Void> {
	private final WorkQueueRestClient workQueueClient;
	private final String workType;
	private final LocalWorker localWorker = new LocalWorker(2);

	/**
	 * Constructeur.
	 * @param workType Type de work
	 * @param workQueueClient Client REST
	 */
	PollWorkTask(final String workType, final WorkQueueRestClient workQueueClient) {
		Assertion.checkArgNotEmpty(workType);
		Assertion.checkNotNull(workQueueClient);
		//---------------------------------------------------------------------
		this.workType = workType;
		this.workQueueClient = workQueueClient;
	}

	/** {@inheritDoc} */
	public Void call() throws Exception {
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
		return null;
	}
}
