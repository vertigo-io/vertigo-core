package io.vertigo.dynamo.work;

import io.vertigo.dynamo.work.WorkResultHandler;

/**
 * Handler unique permettant de collecter les infos relatives à l'exécution des tests.
 * 
 * @author pchretien
 * $Id: MyWorkResultHanlder.java,v 1.3 2013/11/15 15:46:23 pchretien Exp $
 */
public final class MyWorkResultHanlder<WR> implements WorkResultHandler<WR> {
	private WR lastResult;
	private Throwable lastError;
	//compteurs 
	private int succeeded;
	private int failed;
	private final long start = System.currentTimeMillis();

	public synchronized void onStart() {
		//System.out.println("onStart");
	}

	public synchronized WR getLastResult() {
		return lastResult;
	}

	public synchronized Throwable getLastThrowable() {
		return lastError;
	}

	public synchronized void onSuccess(final WR newResult) {
		//System.out.println("onSuccess");
		lastResult = newResult;
		succeeded++;
		fire();
	}

	public synchronized void onFailure(final Throwable newError) {
		//System.out.println("onFailure");
		lastError = newError;
		failed++;
		fire();
	}

	private void fire() {
		if (failed + succeeded > 0 && (failed + succeeded) % 1000 == 0) {
			final long elapsed = System.currentTimeMillis() - start;
			System.out.println(">executed> " + toString() + " in " + 1000 * elapsed / (failed + succeeded) + " ms/1000exec");
		}
	}

	private synchronized boolean isFinished(final int expected, final long timeoutMs) {
		return failed + succeeded < expected && System.currentTimeMillis() - start < timeoutMs;
	}

	public boolean waitFinish(final int expected, final long timeoutMs) {
		while (isFinished(expected, timeoutMs)) {
			try {
				Thread.sleep(100); //On attend 100ms
			} catch (final InterruptedException e) {
				break;//on quitte
			}
		}
		return failed + succeeded == expected;
	}

	@Override
	public synchronized String toString() {
		return "{success : " + succeeded + " , fail : " + failed + " }";

	}
}
