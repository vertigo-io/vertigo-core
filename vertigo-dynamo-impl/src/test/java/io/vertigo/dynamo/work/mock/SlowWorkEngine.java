package io.vertigo.dynamo.work.mock;

import io.vertigo.dynamo.work.WorkEngine;

public final class SlowWorkEngine implements WorkEngine<Boolean, SlowWork> {

	/** {@inheritDoc} */
	public Boolean process(final SlowWork work) {
		try {
			final long sleepTimeAvg = work.getSleepTime();
			final long sleepTimeMax = Math.round(sleepTimeAvg * 1.1d); //+10%
			final long sleepTimeMin = Math.round(sleepTimeAvg * 0.9d); //-10%
			Thread.sleep((sleepTimeMax + sleepTimeMin) / 2);
		} catch (final InterruptedException e) {
			return false;
		}
		return true;
	}
}
