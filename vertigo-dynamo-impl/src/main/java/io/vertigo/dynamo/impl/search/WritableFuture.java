package io.vertigo.dynamo.impl.search;

import io.vertigo.lang.Assertion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * WritableFuture for set result after execution.
 * @see org.apache.http.concurrent.BasicFuture
 * @author npiedeloup
 * @param <V> Result type
 */
public final class WritableFuture<V> implements Future<V> {

	private volatile boolean completed;
	private volatile boolean cancelled;
	private volatile V futureResult;
	private volatile Exception futureException;

	/** {@inheritDoc} */
	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDone() {
		return this.completed;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized V get() throws InterruptedException, ExecutionException {
		while (!this.completed) {
			wait();
		}
		return getResult();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		Assertion.checkNotNull(unit, "Time unit was null");
		//-----
		final long msecs = unit.toMillis(timeout);
		final long startTime = (msecs <= 0) ? 0 : System.currentTimeMillis();
		long waitTime = msecs;
		if (this.completed) {
			return getResult();
		} else if (waitTime <= 0) {
			throw new TimeoutException();
		} else {
			for (;;) {
				wait(waitTime);
				if (this.completed) {
					return getResult();
				}
				waitTime = msecs - (System.currentTimeMillis() - startTime);
				if (waitTime <= 0) {
					throw new TimeoutException();
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
		if (this.completed) {
			return false; //@see Future api
		}
		this.completed = true;
		this.cancelled = true;
		notifyAll();
		return true;
	}

	/**
	 * Mark this execution as success.
	 * @param result Result of execution
	 */
	public synchronized void success(final V result) {
		Assertion.checkState(!this.completed, "Task already completed");
		//-----
		this.completed = true;
		this.futureResult = result;
		notifyAll();
	}

	/**
	 * Mark this execution as failed.
	 * @param exception Failure reason
	 */
	public synchronized void fail(final Exception exception) {
		Assertion.checkState(!this.completed, "Task already completed");
		//-----
		this.completed = true;
		this.futureException = exception;
		notifyAll();
	}

	private V getResult() throws ExecutionException {
		if (futureException != null) {
			throw new ExecutionException(futureException);
		}
		return futureResult;
	}

}
