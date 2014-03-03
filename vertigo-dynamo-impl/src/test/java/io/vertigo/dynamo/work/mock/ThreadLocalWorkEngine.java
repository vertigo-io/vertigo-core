package io.vertigo.dynamo.work.mock;

import io.vertigo.dynamo.work.WorkEngine;

import java.util.HashMap;
import java.util.Map;

public final class ThreadLocalWorkEngine implements WorkEngine<Integer, ThreadLocalWork> {

	private static final ThreadLocal<Map<Integer, String>> threadLocalCache = new ThreadLocal<>();

	public ThreadLocalWorkEngine() {
		if (threadLocalCache.get() == null) {
			threadLocalCache.set(new HashMap<Integer, String>());
		}
	}

	/** {@inheritDoc} */
	public Integer process(final ThreadLocalWork work) {
		final Map<Integer, String> cache = threadLocalCache.get();
		final int size = cache.size();
		final StringBuilder sb = new StringBuilder("aaaa");
		for (int i = 0; i < size; i++) {
			sb.append("Aaaa");
		}
		cache.put(cache.size(), sb.toString());
		try {
			Thread.sleep(work.getSleepTime());
		} catch (final InterruptedException e) {
			//rien
		}
		if (work.getClearThreadLocal()) {
			threadLocalCache.remove();
		}
		return cache.size();
	}
}
