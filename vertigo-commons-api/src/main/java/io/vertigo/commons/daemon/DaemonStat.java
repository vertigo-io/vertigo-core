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
package io.vertigo.commons.daemon;

/**
 *
 * @author pchretien
 */
public interface DaemonStat {

	/** Daemon execution status. */
	enum Status {
		/** Waiting next execution. */
		pending,
		/** Currently running. */
		running;
	}

	/**
	 * @return Daemon name
	 */
	String getDaemonName();

	/**
	 * @return Daemon name
	 */
	Class<? extends Daemon> getDaemonClass();

	/**
	 * @return Daemon period
	 */
	int getDaemonPeriodInSecond();

	/**
	 * @return Nb exec for daemon start
	 */
	long getCount();

	/**
	 * @return Nb successes for daemon start
	 */
	long getSuccesses();

	/**
	 * @return Nb failures for daemon start
	 */
	long getFailures();

	/**
	 * @return Current status
	 */
	Status getStatus();

	/**
	 * @return if last exec was a success
	 */
	boolean isLastExecSuccess();
}
