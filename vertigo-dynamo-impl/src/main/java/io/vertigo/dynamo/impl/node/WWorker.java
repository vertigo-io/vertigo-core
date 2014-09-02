package io.vertigo.dynamo.impl.node;

import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.local.LocalCoordinator;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

final class WWorker implements Runnable {
	private static final int TIMEOUT_IN_SECONDS = 1;
	private final LocalCoordinator localWorker;
	private final String workType;
	private final WorkerPlugin workerPlugin;

	WWorker(/*final String nodeId,*/final String workType, final LocalCoordinator localWorker, final WorkerPlugin nodePlugin) {
		//Assertion.checkArgNotEmpty(nodeId);
		Assertion.checkArgNotEmpty(workType);
		Assertion.checkNotNull(localWorker);
		Assertion.checkNotNull(nodePlugin);
		//-----------------------------------------------------------------
		//	this.nodeId = nodeId;
		this.workType = workType;
		this.localWorker = localWorker;
		this.workerPlugin = nodePlugin;
	}

	/** {@inheritDoc} */
	@Override
	public final void run() {
		while (!Thread.interrupted()) {
			doRun();
		}
	}

	private <WR, W> void doRun() {
		final WorkItem<WR, W> workItem = workerPlugin.<WR, W> pollWorkItem(workType, TIMEOUT_IN_SECONDS);
		if (workItem != null) {

			final Option<WorkResultHandler<WR>> workResultHandler = Option.<WorkResultHandler<WR>> some(new WorkResultHandler<WR>() {
				public void onStart() {
					workerPlugin.putStart(workItem.getId());
				}

				public void onDone(final WR result, final Throwable error) {
					workerPlugin.putResult(workItem.getId(), result, error);
				}
			});
			//---Et on fait executer par le workerLocalredisDB
			localWorker.submit(workItem, workResultHandler);
		}
		//if workitem is null, that's mean there is no workitem available;
	}

}
