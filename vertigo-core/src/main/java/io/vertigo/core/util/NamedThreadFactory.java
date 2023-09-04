package io.vertigo.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private static AtomicInteger threadNumber = new AtomicInteger(1);
	private final ThreadGroup group;
	private final String namePrefix;

	/**
	 * Constructor accepting the prefix of the threads that will be created by this {@link ThreadFactory}.
	 *  @param namePrefix   Prefix for names of threads
	 */
	public NamedThreadFactory(final String namePrefix) {
		group = Thread.currentThread().getThreadGroup();
		this.namePrefix = namePrefix;
	}

	/**
	 * Returns a new thread using a name as specified by this factory.
	 * {@inheritDoc}
	 */
	@Override
	public Thread newThread(final Runnable runnable) {
		//Copy Executors default factory code
		final Thread t = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement());
		if (t.isDaemon()) {
			t.setDaemon(false);
		}
		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;
	}

}
