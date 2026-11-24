/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.daemon.Daemon;
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
	private static final Logger LOG = LogManager.getLogger(DaemonExecutor.class);
	private static final int STOP_TIMEOUT = 30; //30s
	private final AnalyticsManager analyticsManager;
	private boolean isActive;
	private final ScheduledThreadPoolExecutor scheduler;
	private final List<DaemonListener> daemonListeners = new ArrayList<>();

	/**
	 * Creates executor with specified thread pool size.
	 *
	 * @param analyticsManager
	 * @param threadPoolSize Maximum concurrent daemons
	 */
	public DaemonExecutor(final AnalyticsManager analyticsManager, final int threadPoolSize) {
		Assertion.check().isNotNull(analyticsManager);
		//---
		this.analyticsManager = analyticsManager;
		scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(threadPoolSize, new NamedThreadFactory("v-daemon-"));
	}

	private Daemon createTracedDaemon(final DaemonDefinition daemonDefinition, final DaemonListener daemonListener) {
		// the lateness (time spent waiting for a free thread) is measured at start, before the trace
		return () -> analyticsManager.trace(
				"daemon",
				daemonDefinition.getName(),
				tracer -> {
					tracer.setMeasure("lateness", daemonListener.getLastLatenessMillis());
					daemonDefinition.getDaemonSupplier().get().run();
				});
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
		final DaemonListener daemonListener;
		final Daemon daemon;
		if (daemonDefinition.isAnalytics()) {
			// if analytics is enabled (by default) we trace the execution with a tracer
			daemonListener = new DaemonListener(daemonDefinition, false);
			daemon = createTracedDaemon(daemonDefinition, daemonListener);
		} else {
			// otherwise we just execute it
			daemon = daemonDefinition.getDaemonSupplier().get();
			daemonListener = new DaemonListener(daemonDefinition, daemon.verbose());
		}
		final DaemonTimerTask timerTask = new DaemonTimerTask(daemonListener, daemon);
		daemonListeners.add(daemonListener);
		scheduler.scheduleWithFixedDelay(timerTask, daemonDefinition.getPeriodInSeconds(), daemonDefinition.getPeriodInSeconds(), TimeUnit.SECONDS);
	}

	/**
	 * Gets the metrics of the daemon pool : its size, and the executions since the last call (a new window starts).
	 *
	 * @return the daemon pool metrics
	 */
	List<Metric> getPoolMetrics() {
		final DaemonListener.Window window = new DaemonListener.Window();
		daemonListeners.forEach(daemonListener -> window.addAll(daemonListener.takeWindow()));
		//---
		final List<Metric> metrics = new ArrayList<>();
		metrics.add(createMetric("daemonPoolSize", scheduler.getCorePoolSize()));
		metrics.add(createMetric("daemonExecutions", window.executions));
		metrics.add(createMetric("daemonFailures", window.failures));
		if (window.executions > 0) {
			// averages in millis, omitted when there was no execution in the window
			metrics.add(createMetric("daemonAvgDuration", (double) window.totalDurationMillis / window.executions));
			metrics.add(createMetric("daemonAvgLateness", (double) window.totalLatenessMillis / window.executions));
		}
		return metrics;
	}

	private static Metric createMetric(final String name, final double value) {
		return Metric.builder()
				.withSuccess()
				.withName(name)
				.withFeature("daemons")
				.withValue(value)
				.build();
	}

	/**
	 * Gets execution statistics for all daemons.
	 *
	 * @return List of daemon statistics
	 */
	List<DaemonStatImpl> getStats() {
		return daemonListeners
				.stream()
				.map(DaemonListener::getStat)
				.toList();
	}

	/**
	 * Checks whether the underlying scheduler has been shut down.
	 *
	 * @return true if the scheduler is shut down
	 */
	boolean isShutdown() {
		return scheduler.isShutdown();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		isActive = true;
		LOG.info("Daemon pool started with {} threads", scheduler.getCorePoolSize());
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
