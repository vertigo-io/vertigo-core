package io.vertigo.dynamo.work.mock;

import java.io.Serializable;

public final class ThreadLocalWork implements Serializable {
	private static final long serialVersionUID = -1181420471997567103L;
	private final long sleepTime;
	private final boolean clearThreadLocal;

	public ThreadLocalWork(final long sleepTime, final boolean clearThreadLocal) {
		this.sleepTime = sleepTime;
		this.clearThreadLocal = clearThreadLocal;
	}

	long getSleepTime() {
		return sleepTime;
	}

	boolean getClearThreadLocal() {
		return clearThreadLocal;
	}
}
