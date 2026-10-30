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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.daemon.DaemonStat;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;

/**
 * Tracks execution status and statistics for a daemon.
 * Provides thread-safe monitoring of success/failure counts
 * and current execution state.
 */
final class DaemonListener {
	private static final Logger LOG = LogManager.getLogger(DaemonListener.class);
	/** Number of consecutive successes needed by a daemon in failure to be back to normal. */
	private static final int SUCCESSES_TO_RECOVER = 2;

	private long successes;
	private boolean lastExecSucceed;
	private long failures;
	private DaemonStat.Status status = DaemonStat.Status.pending;
	private long startMillis;
	private long dueMillis;
	private long lastLatenessMillis;
	private boolean dead;
	private boolean inFailure;
	private int successesSinceFailure;
	private Window window = new Window();
	private final DaemonDefinition daemonDefinition;
	private final boolean verbose;

	/**
	 * Executions over a window : since the last read.
	 * Not thread-safe : a window is updated under the lock of its listener, or confined to its reader.
	 */
	static final class Window {
		long executions;
		long failures;
		long totalDurationMillis;
		long totalLatenessMillis;

		void add(final long durationMillis, final long latenessMillis, final boolean success) {
			executions++;
			if (!success) {
				failures++;
			}
			totalDurationMillis += durationMillis;
			totalLatenessMillis += latenessMillis;
		}

		void addAll(final Window other) {
			executions += other.executions;
			failures += other.failures;
			totalDurationMillis += other.totalDurationMillis;
			totalLatenessMillis += other.totalLatenessMillis;
		}
	}

	/**
	 * Creates a new listener for the specified daemon.
	 *
	 * @param daemonDefinition Daemon configuration
	 * @param verbose Whether to log detailed execution info
	 */
	DaemonListener(final DaemonDefinition daemonDefinition, final boolean verbose) {
		Assertion.check().isNotNull(daemonDefinition);
		//---
		this.daemonDefinition = daemonDefinition;
		this.verbose = verbose;
		// with a fixed delay, the first execution is due one period after the registration
		dueMillis = System.currentTimeMillis() + getPeriodMillis();
	}

	/**
	 * Gets current execution statistics.
	 * Creates an immutable snapshot of current state.
	 *
	 * @return Current daemon statistics
	 */
	synchronized DaemonStatImpl getStat() {
		//On copie les données
		return new DaemonStatImpl(daemonDefinition, successes, failures, status, lastExecSucceed, startMillis, dueMillis, dead, inFailure);
	}

	/**
	 * Gets the executions since the last call, and starts a new window.
	 *
	 * @return the window
	 */
	synchronized Window takeWindow() {
		final Window takenWindow = window;
		window = new Window();
		return takenWindow;
	}

	/**
	 * Gets the lateness of the last start : how long it waited for a free thread.
	 *
	 * @return the lateness in millis
	 */
	synchronized long getLastLatenessMillis() {
		return lastLatenessMillis;
	}

	/**
	 * Records daemon execution start.
	 * Updates status and logs if verbose enabled.
	 */
	synchronized void onStart() {
		status = DaemonStat.Status.running;
		startMillis = System.currentTimeMillis();
		lastLatenessMillis = Math.max(0L, startMillis - dueMillis);
		if (verbose) {
			LOG.info("Start daemon: {}", daemonDefinition.getName());
		}
	}

	/**
	 * Records that the daemon's periodic schedule was cancelled by a fatal Error: no future execution will occur.
	 *
	 * @param e Error that killed the daemon
	 */
	synchronized void onDead(final Error e) {
		dead = true;
		LOG.error("Daemon [{}] crashed with {}: the periodic schedule is cancelled, no future execution will occur",
				daemonDefinition.getName(), e.getClass().getSimpleName(), e);
	}

	/**
	 * Records daemon execution failure.
	 * Updates counters, status and logs error.
	 *
	 * @param e Exception that caused the failure
	 */
	synchronized void onFailure(final Exception e) {
		failures++;
		lastExecSucceed = false;
		inFailure = true;
		successesSinceFailure = 0;
		onEnd(false);
		LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonDefinition.id(), e);
	}

	/**
	 * Records successful daemon execution.
	 * Updates counters, status and logs if verbose enabled.
	 */
	synchronized void onSuccess() {
		successes++;
		lastExecSucceed = true;
		// hysteresis : a daemon in failure is back to normal after two consecutive successes
		if (inFailure && ++successesSinceFailure >= SUCCESSES_TO_RECOVER) {
			inFailure = false;
		}
		onEnd(true);
		if (verbose) {
			LOG.info("Execution succeeded on daemon: {}", daemonDefinition.id());
		}
	}

	private void onEnd(final boolean success) {
		status = DaemonStat.Status.pending;
		final long endMillis = System.currentTimeMillis();
		// with a fixed delay, the next execution is due one period after the end of this one
		dueMillis = endMillis + getPeriodMillis();
		window.add(endMillis - startMillis, lastLatenessMillis, success);
		checkAndWarnExecutionTime(endMillis - startMillis);
	}

	private long getPeriodMillis() {
		return daemonDefinition.getPeriodInSeconds() * 1000L;
	}

	/**
	 * Warns if daemon execution exceeded its configured period.
	 */
	private void checkAndWarnExecutionTime(final long duration) {
		final long periodMs = getPeriodMillis();
		if (duration > periodMs) {
			LOG.warn("Daemon [{}] took {}ms (> period {}ms), risking daemon thread starvation",
					daemonDefinition.getName(), duration, periodMs);
		}
	}
}
