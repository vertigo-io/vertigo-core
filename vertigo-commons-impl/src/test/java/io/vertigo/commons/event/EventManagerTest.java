package io.vertigo.commons.event;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author npiedeloup
 */
public final class EventManagerTest extends AbstractTestCaseJU4 {

	@Inject
	private EventManager eventManager;
	private final EventChannel<String> eventChannel1 = new EventChannel<String>() {
		//rien
	};

	private final EventChannel<String> eventChannel2 = new EventChannel<String>() {
		//rien
	};

	@Test
	public void testSimple() throws InterruptedException, ExecutionException {
		final MyFuture<String> result = new MyFuture<>(1);
		eventManager.register(eventChannel1, new MyEventListener(result));

		Assert.assertFalse(result.isDone());
		eventManager.fire(eventChannel1, "Test1");
		Assert.assertTrue(result.isDone());
		Assert.assertEquals("Test1", result.get().get(0));
	}

	@Test
	public void testMultipleEvent() throws InterruptedException, ExecutionException {
		final MyFuture<String> result1 = new MyFuture<>(10);
		eventManager.register(eventChannel1, new MyEventListener(result1));

		Assert.assertFalse(result1.isDone());
		for (int i = 0; i < 9; i++) {
			eventManager.fire(eventChannel1, "Test1");
			Assert.assertFalse(result1.isDone());
		}
		eventManager.fire(eventChannel1, "Test1");
		Assert.assertTrue(result1.isDone());
		Assert.assertEquals(10, result1.get().size());
	}

	@Test
	public void testConcurrentChannel() throws InterruptedException, ExecutionException {
		final MyFuture<String> result1 = new MyFuture<>(1);
		final MyFuture<String> result2 = new MyFuture<>(1);
		eventManager.register(eventChannel1, new MyEventListener(result1));
		eventManager.register(eventChannel2, new MyEventListener(result2));

		Assert.assertFalse(result1.isDone());
		Assert.assertFalse(result2.isDone());
		eventManager.fire(eventChannel1, "Test1");
		Assert.assertTrue(result1.isDone());
		Assert.assertFalse(result2.isDone());
		Assert.assertEquals("Test1", result1.get().get(0));
		eventManager.fire(eventChannel2, "Test2");
		Assert.assertTrue(result1.isDone());
		Assert.assertTrue(result2.isDone());
	}

	@Test
	public void testMultipleListener() throws InterruptedException, ExecutionException {
		final MyFuture<String> result1 = new MyFuture<>(1);
		final MyFuture<String> result2 = new MyFuture<>(1);
		eventManager.register(eventChannel1, new MyEventListener(result1));
		eventManager.register(eventChannel1, new MyEventListener(result2));

		Assert.assertFalse(result1.isDone());
		Assert.assertFalse(result2.isDone());
		eventManager.fire(eventChannel1, "Test1");
		Assert.assertTrue(result1.isDone());
		Assert.assertTrue(result2.isDone());
		Assert.assertEquals("Test1", result1.get().get(0));
		Assert.assertEquals("Test1", result2.get().get(0));
	}

	private static final class MyEventListener<O extends Serializable> implements EventListener<O> {
		private final MyFuture<O> result;

		public MyEventListener(final MyFuture<O> result) {
			this.result = result;
		}

		@Override
		public void onEvent(final Event<O> event) {
			result.receivedValue(event.getPayload());
		}
	}

	private static final class MyFuture<O> implements Future<List<O>> {
		private final List<O> value = new ArrayList<>();
		private final int expectedCount;

		public MyFuture(final int expectedCount) {
			this.expectedCount = expectedCount;
		}

		@Override
		public boolean cancel(final boolean mayInterruptIfRunning) {
			throw new UnsupportedOperationException("No supported");
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return value.size() >= expectedCount;
		}

		@Override
		public List<O> get() throws InterruptedException, ExecutionException {
			return value;
		}

		public void receivedValue(final O element) {
			Assertion.checkNotNull(element);
			this.value.add(element);
		}

		@Override
		public List<O> get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			throw new UnsupportedOperationException("No supported");
		}
	}
}
