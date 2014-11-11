/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	@Override
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
