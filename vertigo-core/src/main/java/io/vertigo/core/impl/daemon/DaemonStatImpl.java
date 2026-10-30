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

import io.vertigo.core.daemon.DaemonStat;
import io.vertigo.core.daemon.definitions.DaemonDefinition;
import io.vertigo.core.lang.Assertion;

/**
 * Immutable snapshot of daemon execution statistics.
 * Provides thread-safe access to execution metrics by capturing
 * point-in-time values rather than live state.
 *
 * @author pchretien
 */
final class DaemonStatImpl implements DaemonStat {
	private final DaemonDefinition daemonDefinition;
	private final DaemonStat.Status status;
	private final long sucesses;
	private final long failures;
	private final boolean lastExecSuccess;
	private final long startMillis;
	private final long dueMillis;
	private final boolean dead;
	private final boolean inFailure;

	/**
	 * Creates a new statistics snapshot.
	 *
	 * @param daemonDefinition Daemon configuration
	 * @param successes Number of successful executions
	 * @param failures Number of failed executions
	 * @param status Current execution status
	 * @param lastExecSuccess Whether last execution succeeded
	 * @param startMillis Start of the current execution (meaningful when running)
	 * @param dueMillis Expected start of the next execution : one period after the end of the last execution (or after the registration)
	 * @param dead Whether the periodic schedule was cancelled by a fatal Error (the daemon will never run again)
	 * @param inFailure Whether the daemon is in failure : from a failure until two consecutive successes
	 */
	DaemonStatImpl(
			final DaemonDefinition daemonDefinition,
			final long successes,
			final long failures,
			final DaemonStat.Status status,
			final boolean lastExecSuccess,
			final long startMillis,
			final long dueMillis,
			final boolean dead,
			final boolean inFailure) {
		Assertion.check()
				.isNotNull(daemonDefinition)
				.isNotNull(status);
		//----
		this.daemonDefinition = daemonDefinition;
		this.failures = failures;
		sucesses = successes;
		this.status = status;
		this.lastExecSuccess = lastExecSuccess;
		this.startMillis = startMillis;
		this.dueMillis = dueMillis;
		this.dead = dead;
		this.inFailure = inFailure;
	}

	/** {@inheritDoc} */
	@Override
	public String getDaemonName() {
		return daemonDefinition.getName();
	}

	/** {@inheritDoc} */
	@Override
	public int getDaemonPeriodInSecond() {
		return daemonDefinition.getPeriodInSeconds();
	}

	/** {@inheritDoc} */
	@Override
	public long getCount() {
		return sucesses + failures;
	}

	/** {@inheritDoc} */
	@Override
	public long getSuccesses() {
		return sucesses;
	}

	/** {@inheritDoc} */
	@Override
	public long getFailures() {
		return failures;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLastExecSuccess() {
		return lastExecSuccess;
	}

	/** {@inheritDoc} */
	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * @return Start of the current execution in epoch millis (meaningful when running)
	 */
	long getStartMillis() {
		return startMillis;
	}

	/**
	 * @return Expected start of the next execution in epoch millis
	 */
	long getDueMillis() {
		return dueMillis;
	}

	/**
	 * @return Whether the periodic schedule was cancelled by a fatal Error
	 */
	boolean isDead() {
		return dead;
	}

	/**
	 * @return Whether the daemon is in failure (from a failure until two consecutive successes)
	 */
	boolean isInFailure() {
		return inFailure;
	}
}
