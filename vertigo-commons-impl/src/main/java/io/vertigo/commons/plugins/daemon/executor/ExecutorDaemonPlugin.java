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
package io.vertigo.commons.plugins.daemon.executor;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.impl.daemon.DaemonPlugin;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Implémentation basic du plugin de gestion de démon.
 *
 * @author TINGARGIOLA
 */
public final class ExecutorDaemonPlugin implements DaemonPlugin, Activeable {
	private boolean isActive;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

	/** {@inheritDoc} */
	@Override
	public void scheduleDaemon(final String daemonName, final Daemon daemon, final long periodInSeconds) {
		Assertion.checkNotNull(daemon);
		Assertion.checkState(isActive, "Le manager n'est pas actif.");
		// -----
		scheduler.scheduleWithFixedDelay(new MyTimerTask(daemonName, daemon), 0, periodInSeconds, TimeUnit.SECONDS);
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		isActive = true;
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		scheduler.shutdown();
		isActive = false;
	}

	private static class MyTimerTask implements Runnable {

		private static final Logger LOG = Logger.getLogger(MyTimerTask.class);
		private final Daemon daemon;
		private final String daemonName;

		MyTimerTask(final String daemonName, final Daemon daemon) {
			Assertion.checkArgNotEmpty(daemonName);
			Assertion.checkNotNull(daemon);
			// -----
			this.daemon = daemon;
			this.daemonName = daemonName;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			try {//try catch needed to ensure execution aren't suppressed
				LOG.info("Start daemon: " + daemonName);
				daemon.run();
				LOG.info("Executio succeeded on daemon: " + daemonName);
			} catch (final Exception e) {
				LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonName, e);
			}
		}
	}
}
