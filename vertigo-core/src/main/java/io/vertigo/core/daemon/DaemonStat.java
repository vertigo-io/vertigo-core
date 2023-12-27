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
package io.vertigo.core.daemon;

/**
 * Represents execution statistics about registered daemons.
 *
 * This interface provides detailed statistics about the execution of registered daemons.
 * 
 * @author: pchretien
 */
public interface DaemonStat {
	/**
     * Enumeration representing the status of daemon execution.
     */
	enum Status {
		  /** Waiting for the next execution. */
		pending,
		 /** Currently running. */
		running
	}

	 /**
     * Gets the name of the daemon.
     *
     * @return the name of the daemon.
     */
	String getDaemonName();

	 /**
     * Gets the period (in seconds) at which the daemon is configured to run.
     *
     * @return the period (in seconds) at which the daemon is configured to run.
     */
	int getDaemonPeriodInSecond();

	 /**
     * Gets the total number of executions since the daemon started.
     *
     * @return the total number of executions since the daemon started.
     */
	long getCount();

    /**
     * Gets the number of successful executions since the daemon started.
     *
     * @return the number of successful executions since the daemon started.
     */
	long getSuccesses();

	/**
     * Gets the number of failures since the daemon started.
     *
     * @return the number of failures since the daemon started.
     */
	long getFailures();

	 /**
     * Gets the current status of the daemon execution.
     *
     * @return the current status of the daemon execution.
     */
	Status getStatus();

	/**
     * Checks if the last execution of the daemon was successful.
     *
     * @return {@code true} if the last execution was successful, {@code false} otherwise.
     */
	boolean isLastExecSuccess();
}
