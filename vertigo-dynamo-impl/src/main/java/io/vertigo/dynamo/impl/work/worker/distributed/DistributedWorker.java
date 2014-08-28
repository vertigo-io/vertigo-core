package io.vertigo.dynamo.impl.work.worker.distributed;

import io.vertigo.dynamo.impl.work.MasterPlugin;
import io.vertigo.dynamo.impl.work.MasterPlugin.WCallback;
import io.vertigo.dynamo.impl.work.WResult;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author pchretien
 */
public final class DistributedWorker implements Worker, WCallback {
	private final MasterPlugin masterPlugin;
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());

	public DistributedWorker(final MasterPlugin masterPlugin) {
		Assertion.checkNotNull(masterPlugin);
		//---------------------------------------------------------------------
		this.masterPlugin = masterPlugin;
		masterPlugin.registerCallback(this);
	}

	/** {@inheritDoc} */
	public <WR, W> Future<WR> submit(final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		//1. On renseigne la demande de travaux sur le server redis
		masterPlugin.putWorkItem(workItem);
		//2. On attend les notifs sur un thread séparé, la main est rendue de suite
		return createFuture(workItem.getId(), workResultHandler);
	}

	private <WR, W> Future<WR> createFuture(final String workId, final Option<WorkResultHandler<WR>> workResultHandler) {
		Assertion.checkNotNull(workId);
		//---------------------------------------------------------------------
		final WFuture<WR> future;
		if (workResultHandler.isDefined()) {
			future = new WFuture<>(workResultHandler.get());
		} else {
			future = new WFuture<>();
		}
		workResultHandlers.put(workId, future);
		return future;
	}

	/** {@inheritDoc} */
	public final void setResult(final WResult result) {
		final WorkResultHandler workResultHandler = workResultHandlers.remove(result.getWorkId());
		if (workResultHandler != null) {
			//Que faire sinon
			workResultHandler.onDone(result.hasSucceeded(), result.getResult(), result.getError());
		}
	}

	/**
	 * Indique si ce type de work peut-être distribué.
	 * @param work Travail à effectuer
	 * @return si ce type de work peut-être distribué.
	 */
	public <WR, W> boolean accept(final WorkItem<WR, W> workItem) {
		return masterPlugin.acceptedWorkTypes().contains(workItem.getWorkType());
	}
}
