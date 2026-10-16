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

	private long successes;
	private boolean lastExecSucceed;
	private long failures;
	private DaemonStat.Status status = DaemonStat.Status.pending;
	private long startMillis;
	private final long registeredMillis;
	private long lastEndMillis;
	private long lastLatenessMillis;
	private long consecutiveFailures;
	private boolean dead;
	private final DaemonDefinition daemonDefinition;
	private final boolean verbose;

	/**
	 * Creates a new listener for the specified daemon.
	 *
	 * @param daemonDefinition Daemon configuration
	 * @param verbose Whether to log detailed execution info
	 */
	DaemonListener(final DaemonDefinition daemonDefinition, final boolean verbose) {
		Assertion.check().isNotNull(daemonDefinition);
		//---
		this.registeredMillis = System.currentTimeMillis();
		this.daemonDefinition = daemonDefinition;
		this.verbose = verbose;
	}

	/**
	 * Expected start of the next execution: with a fixed delay, each execution is due one period
	 * after the end of the previous execution (or after registration for the first execution).
	 *
	 * @return the due time in epoch millis
	 */
	private long getDueMillis() {
		final long lastExecutionEnd = lastEndMillis > 0 ? lastEndMillis : registeredMillis;
		return lastExecutionEnd + daemonDefinition.getPeriodInSeconds() * 1000L;
	}

	/**
	 * Gets how long the next execution is overdue: how long a due execution has been waiting for a free thread.
	 *
	 * @return the overdue duration in millis, or 0 if the daemon is not overdue
	 */
	synchronized long getOverdueMillis() {
		final long nowMillis = System.currentTimeMillis();
		final long dueMillis = getDueMillis();
		return status == DaemonStat.Status.pending && nowMillis >= dueMillis ? nowMillis - dueMillis : 0L;
	}

	/**
	 * Gets the name of the daemon.
	 *
	 * @return the daemon name
	 */
	String getDaemonName() {
		return daemonDefinition.getName();
	}

	/**
	 * Gets the configured period in millis.
	 *
	 * @return the period in millis
	 */
	long getPeriodMillis() {
		return daemonDefinition.getPeriodInSeconds() * 1000L;
	}

	/**
	 * Gets current execution statistics.
	 * Creates an immutable snapshot of current state.
	 * 
	 * @return Current daemon statistics
	 */
	synchronized DaemonStat getStat() {
		//On copie les données
		return new DaemonStatImpl(daemonDefinition, successes, failures, status, lastExecSucceed,
				status == DaemonStat.Status.running ? startMillis : 0, lastEndMillis, lastLatenessMillis, consecutiveFailures);
	}

	/**
	 * Records daemon execution start.
	 * Updates status and logs if verbose enabled.
	 */
	synchronized void onStart() {
		final long nowMillis = System.currentTimeMillis();
		final long periodMillis = daemonDefinition.getPeriodInSeconds() * 1000L;
		lastLatenessMillis = Math.max(0L, nowMillis - getDueMillis());
		if (lastLatenessMillis > periodMillis) {
			LOG.warn("Daemon [{}] started {}ms late (> period {}ms), pool may be saturated",
					daemonDefinition.getName(), lastLatenessMillis, periodMillis);
		}
		//---
		status = DaemonStat.Status.running;
		startMillis = nowMillis;
		if (verbose) {
			LOG.info("Start daemon: {}", daemonDefinition.getName());
		}
	}

	/**
	 * Records that the daemon's periodic schedule was cancelled by a fatal Error: no future execution will occur.
	 */
	synchronized void onDead() {
		dead = true;
	}

	/**
	 * Checks whether the daemon is dead: its periodic schedule was cancelled by a fatal Error.
	 *
	 * @return true if the daemon will never run again
	 */
	synchronized boolean isDead() {
		return dead;
	}

	/**
	 * Records daemon execution failure.
	 * Updates counters, status and logs error.
	 *
	 * @param e Exception that caused the failure
	 */
	synchronized void onFailure(final Exception e) {
		status = DaemonStat.Status.pending;
		lastEndMillis = System.currentTimeMillis();
		failures++;
		consecutiveFailures++;
		lastExecSucceed = false;
		checkAndWarnExecutionTime();
		LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonDefinition.id(), e);
	}

	/**
	 * Records successful daemon execution.
	 * Updates counters, status and logs if verbose enabled.
	 */
	synchronized void onSuccess() {
		status = DaemonStat.Status.pending;
		lastEndMillis = System.currentTimeMillis();
		successes++;
		consecutiveFailures = 0L;
		lastExecSucceed = true;
		checkAndWarnExecutionTime();
		if (verbose) {
			LOG.info("Execution succeeded on daemon: {}", daemonDefinition.id());
		}
	}

	/**
	 * Warns if daemon execution exceeded its configured period.
	 */
	private void checkAndWarnExecutionTime() {
		final long duration = System.currentTimeMillis() - startMillis;
		final long periodMs = daemonDefinition.getPeriodInSeconds() * 1000L;
		if (duration > periodMs) {
			LOG.warn("Daemon [{}] took {}ms (> period {}ms), risking daemon thread starvation",
					daemonDefinition.getName(), duration, periodMs);
		}
	}
}
