/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.impl.daemon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.vertigo.core.daemon.Daemon;
import io.vertigo.core.daemon.DaemonStat;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.util.NamedThreadFactory;

/**
 * This class executes the daemons that have been previously registered.
 *
 * @author mlaroche, pchretien, npiedeloup
 */
final class DaemonExecutor implements Activeable {
	private static final int STOP_TIMEOUT = 30; //30s
	private boolean isActive;
	private final ScheduledExecutorService scheduler;
	private final List<DaemonListener> daemonListeners = new ArrayList<>();

	/**
	 * Constructor.
	 * @param threadPoolSize thread pool size
	 */
	public DaemonExecutor(final int threadPoolSize) {
		scheduler = Executors.newScheduledThreadPool(threadPoolSize, new NamedThreadFactory("v-daemon-"));
	}

	private static Daemon createDaemon(final DaemonDefinition daemonDefinition) {
		return daemonDefinition.getDaemonSupplier().get();
	}

	/**
	* Registers a new daemon.
	* It will be executed after the delay (in milliseconds)
	* and will be periodically executed after the period (in milliseconds)
	*
	* @param daemonDefinition the daemon definition
	* @param daemon Daemon to schedule.
	*/
	void scheduleDaemon(final DaemonDefinition daemonDefinition) {
		Assertion.check()
				.isNotNull(daemonDefinition)
				.isTrue(isActive, "Manager must be active to schedule a daemon");
		// -----
		final Daemon daemon = createDaemon(daemonDefinition);
		final DaemonListener daemonListener = new DaemonListener(daemonDefinition, daemon.verbose());
		final DaemonTimerTask timerTask = new DaemonTimerTask(daemonListener, daemon);
		daemonListeners.add(daemonListener);
		scheduler.scheduleWithFixedDelay(timerTask, daemonDefinition.getPeriodInSeconds(), daemonDefinition.getPeriodInSeconds(), TimeUnit.SECONDS);

		//look at https://stackoverflow.com/questions/20279736/java-scheduleexecutorservice-timeout-task
	}

	/**
	 * @return Daemons stats
	 */
	List<DaemonStat> getStats() {
		return daemonListeners
				.stream()
				.map(DaemonListener::getStat)
				.toList();
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
		try {
			scheduler.awaitTermination(STOP_TIMEOUT, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			throw WrappedException.wrap(e);
		}
	}
}
