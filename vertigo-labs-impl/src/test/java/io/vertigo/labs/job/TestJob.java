package io.vertigo.labs.job;

import io.vertigo.kernel.exception.VRuntimeException;

public final class TestJob implements Runnable {
	private static int count = 0;

	public void run() {
		try {
			//On simule une attente qui correspond � un traitement m�tier de 100 ms
			Thread.sleep(100);
		} catch (final InterruptedException e) {
			throw new VRuntimeException(e);
		}
		incCount();
	}

	private synchronized void incCount() {
		count++;
	}

	public static synchronized int getCount() {
		return count;
	}

	public static synchronized void reset() {
		count = 0;
	}
}
