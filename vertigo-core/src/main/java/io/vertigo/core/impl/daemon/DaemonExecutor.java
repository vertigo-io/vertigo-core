/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.daemon.Daemon;
import io.vertigo.core.daemon.DaemonStat;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.NamedThreadFactory;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.Activeable;

/**
 * Manages execution of registered daemons using a thread pool.
 * Handles scheduling, monitoring and lifecycle of daemon processes.
 *
 * @author mlaroche, pchretien, npiedeloup
 */
final class DaemonExecutor implements Activeable {
	private static final int STOP_TIMEOUT = 30; //30s
	private final AnalyticsManager analyticsManager;
	private boolean isActive;
	private final ScheduledExecutorService scheduler;
	private final List<DaemonListener> daemonListeners = new ArrayList<>();

	/**
	 * Creates executor with specified thread pool size.
	 *
	 * @param analyticsManager
	 * @param threadPoolSize Maximum concurrent daemons
	 */
	public DaemonExecutor(final AnalyticsManager analyticsManager, final int threadPoolSize) {
		this.analyticsManager = analyticsManager;
		scheduler = Executors.newScheduledThreadPool(threadPoolSize, new NamedThreadFactory("v-daemon-"));
	}

	private Daemon createDaemon(final DaemonDefinition daemonDefinition) {
		if (daemonDefinition.isAnalytics()) {
			// if analytics is enabled (by default) we trace the execution with a tracer
			return () -> analyticsManager.trace(
					"daemon",
					daemonDefinition.getName(),
					tracer -> daemonDefinition.getDaemonSupplier().get().run());
		}
		// otherwise we just execute it
		return daemonDefinition.getDaemonSupplier().get();
	}

	/**
	 * Schedules periodic execution of a daemon.
	 * Daemon will run at fixed intervals defined in its definition.
	 *
	 * @param daemonDefinition Configuration for the daemon
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
	}

	/**
	 * Gets execution statistics for all daemons.
	 *
	 * @return List of daemon statistics
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
