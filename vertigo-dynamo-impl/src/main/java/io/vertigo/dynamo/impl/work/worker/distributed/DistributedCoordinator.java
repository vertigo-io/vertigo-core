package io.vertigo.dynamo.impl.work.worker.distributed;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.impl.work.MasterPlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.Coordinator;
import io.vertigo.dynamo.work.WorkResultHandler;

import java.util.concurrent.Future;

/**
 * @author pchretien
 */
public final class DistributedCoordinator implements Coordinator {
	private final MasterPlugin masterPlugin;

	public DistributedCoordinator(final MasterPlugin masterPlugin) {
		Assertion.checkNotNull(masterPlugin);
		//---------------------------------------------------------------------
		this.masterPlugin = masterPlugin;
	}

	/** {@inheritDoc} */
	public <WR, W> Future<WR> submit(final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		//2. On attend les notifs sur un thread séparé, la main est rendue de suite
		final WFuture<WR> future = createFuture(workItem.getId(), workResultHandler);
		masterPlugin.putWorkItem(workItem, future);
		return future;
	}

	private <WR, W> WFuture<WR> createFuture(final String workId, final Option<WorkResultHandler<WR>> workResultHandler) {
		Assertion.checkNotNull(workId);
		//---------------------------------------------------------------------
		final WFuture<WR> future;
		if (workResultHandler.isDefined()) {
			future = new WFuture<>(workResultHandler.get());
		} else {
			future = new WFuture<>();
		}
		return future;
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
