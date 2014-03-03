package io.vertigo.dynamo.work.mock;

import java.io.Serializable;

public final class SlowWork implements Serializable {
	private static final long serialVersionUID = -5638180614179332598L;
	private final long sleepTime;

	public SlowWork(final long sleepTime) {
		this.sleepTime = sleepTime;
	}

	long getSleepTime() {
		return sleepTime;
	}
}
