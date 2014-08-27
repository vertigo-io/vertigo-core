package io.vertigo.dynamo.plugins.work.rest.worker;

import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

import java.util.concurrent.Callable;

final class ReScheduleWorkResultHandler implements WorkResultHandler<Void> {
	private final Callable<Void> task;
	private final long pauseMs;
	private final WorkManager workManager;

	public ReScheduleWorkResultHandler(final Callable<Void> task, final long pauseMs, final WorkManager workManager) {
		Assertion.checkNotNull(task);
		Assertion.checkArgument(pauseMs >= 0 && pauseMs < 1000000, "La pause est exprimé en millisecond et est >=0 et < 1000000");
		Assertion.checkNotNull(workManager);
		//-----------------------------------------------------------------
		this.task = task;
		this.pauseMs = pauseMs;
		this.workManager = workManager;
	}

	/** {@inheritDoc} */
	public void onStart() {
		//rien
	}

	/** {@inheritDoc} */
	public void onDone(final boolean suceeded, final Void result, final Throwable error) {
		reSchedule();
	}

	private void reSchedule() {
		try {
			Thread.sleep(pauseMs);
			workManager.schedule(task, this);
		} catch (final InterruptedException e) {
			//rien on stop
		}
	}
}
