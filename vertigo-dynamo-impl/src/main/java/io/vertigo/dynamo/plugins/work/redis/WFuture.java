package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pchretien
 * $Id: RedisDispatcherThread.java,v 1.8 2014/02/03 17:28:45 pchretien Exp $
 */
public final class WFuture<WR> implements Future<WR>, WorkResultHandler<WR> {
	private final AtomicBoolean done = new AtomicBoolean(false);
	private final CountDownLatch countDownLatch = new CountDownLatch(1);
	private Throwable myError;
	private WR myResult;
	private final WorkResultHandler<WR> redirect;

	public WFuture(final WorkResultHandler<WR> redirect) {
		this.redirect = redirect;
	}

	public WFuture() {
		redirect = null;
	}

	//	public WFuture(final WR result) {
	//		Assertion.checkNotNull(result);
	//		//---------------------------------------------------------------------
	//		this.myResult = result;
	//		this.myError = null;
	//		done.set(true);
	//		countDownLatch.countDown();
	//	}
	//
	//	public WFuture(final Throwable error) {
	//		Assertion.checkNotNull(error);
	//		//---------------------------------------------------------------------
	//		this.myResult = null;
	//		this.myError = error;
	//		done.set(true);
	//		countDownLatch.countDown();
	//	}

	public void onSuccess(final WR result) {
		Assertion.checkNotNull(result);
		//---------------------------------------------------------------------
		if (done.compareAndSet(false, true)) {
			myResult = result;
			countDownLatch.countDown();
		}
		if (redirect != null) {
			redirect.onSuccess(result);
		}
	}

	public void onStart() {
		if (redirect != null) {
			redirect.onStart();
		}
	}

	public void onFailure(final Throwable error) {
		Assertion.checkNotNull(error);
		//---------------------------------------------------------------------
		if (done.compareAndSet(false, true)) {
			myError = error;
			countDownLatch.countDown();
		}
		if (redirect != null) {
			redirect.onFailure(error);
		}
	}

	public boolean cancel(final boolean mayInterruptIfRunning) {
		if (done.compareAndSet(false, true)) {
			myResult = null;
			myError = new CancellationException();
			countDownLatch.countDown();
			return true;
		}
		return false;
	}

	public boolean isCancelled() {
		if (done.get()) {
			try {
				countDownLatch.await();
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
			return myError instanceof CancellationException;
		}
		return false;
	}

	public boolean isDone() {
		return done.get() && countDownLatch.getCount() == 0;
	}

	public WR get() throws InterruptedException, ExecutionException {
		countDownLatch.await();
		if (myResult != null) {
			return myResult;
		}
		if (myError instanceof CancellationException) {
			throw (CancellationException) new CancellationException().initCause(myError);
		}
		throw new ExecutionException(myError);
	}

	public WR get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (!countDownLatch.await(timeout, unit)) {
			throw new TimeoutException();
		}
		if (myResult != null) {
			return myResult;
		}
		if (myError instanceof CancellationException) {
			throw (CancellationException) new CancellationException().initCause(myError);
		}
		throw new ExecutionException(myError);
	}

	//	public static void rethrow(final ExecutionException e) throws IOException {
	//		final Throwable cause = e.getCause();
	//		if (cause instanceof IOException)
	//			throw (IOException) cause;
	//		if (cause instanceof Error)
	//			throw (Error) cause;
	//		if (cause instanceof RuntimeException)
	//			throw (RuntimeException) cause;
	//		throw new RuntimeException(cause);
	//	}
}
