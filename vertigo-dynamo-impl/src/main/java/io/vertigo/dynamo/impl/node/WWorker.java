package io.vertigo.dynamo.impl.node;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.plugins.work.WResult;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

final class WWorker implements Runnable {
	private static final int TIMEOUT_IN_SECONDS = 1;
	private final LocalWorker localWorker;
	private  final String workType;
	private final NodePlugin nodePlugin;
	WWorker(/*final String nodeId,*/ final String workType, final LocalWorker localWorker, final NodePlugin nodePlugin) {
		//Assertion.checkArgNotEmpty(nodeId);
		Assertion.checkArgNotEmpty(workType);
		Assertion.checkNotNull(localWorker);
		Assertion.checkNotNull(nodePlugin);
		//-----------------------------------------------------------------
		//	this.nodeId = nodeId;
		this.workType = workType;
		this.localWorker = localWorker;
		this.nodePlugin = nodePlugin;
	}

	/** {@inheritDoc} */
	@Override
	public final void run() {
		while (!Thread.interrupted()) {
			doRun();
		}
	}

	private <WR, W> void doRun() {
		final WorkItem<WR, W> workItem = nodePlugin.<WR,W>pollWorkItem(workType, TIMEOUT_IN_SECONDS);
		if (workItem != null) {

			final Option<WorkResultHandler<WR>> workResultHandler = Option.<WorkResultHandler<WR>> some(new WorkResultHandler<WR>() {
				public void onStart() {
					nodePlugin.putStart(workItem.getId());
				}

				public void onDone(final boolean succeeded, final WR result, final Throwable error) {
					nodePlugin.putResult(new WResult(workItem.getId(), succeeded, result, error));
				}
			});
			//---Et on fait executer par le workerLocalredisDB
			localWorker.submit(workItem, workResultHandler);
		}
		//if workitem is null, that's mean there is no workitem available;
	}

}