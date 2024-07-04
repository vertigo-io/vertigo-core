/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2024, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang;

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
