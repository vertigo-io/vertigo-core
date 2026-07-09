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
		this.daemonDefinition = daemonDefinition;
		this.verbose = verbose;
	}

	/**
	 * Gets current execution statistics.
	 * Creates an immutable snapshot of current state.
	 * 
	 * @return Current daemon statistics
	 */
	synchronized DaemonStat getStat() {
		//On copie les données
		return new DaemonStatImpl(daemonDefinition, successes, failures, status, lastExecSucceed);
	}

	/**
	 * Records daemon execution start.
	 * Updates status and logs if verbose enabled.
	 */
	synchronized void onStart() {
		status = DaemonStat.Status.running;
		if (verbose) {
			LOG.info("Start daemon: {}", daemonDefinition.getName());
		}
	}

	/**
	 * Records daemon execution failure.
	 * Updates counters, status and logs error.
	 * 
	 * @param e Exception that caused the failure
	 */
	synchronized void onFailure(final Exception e) {
		status = DaemonStat.Status.pending;
		failures++;
		lastExecSucceed = false;
		LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonDefinition.id(), e);
	}

	/**
	 * Records successful daemon execution.
	 * Updates counters, status and logs if verbose enabled.
	 */
	synchronized void onSuccess() {
		status = DaemonStat.Status.pending;
		successes++;
		lastExecSucceed = true;
		if (verbose) {
			LOG.info("Execution succeeded on daemon: {}", daemonDefinition.id());
		}
	}
}
