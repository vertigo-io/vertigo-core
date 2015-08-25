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
package io.vertigo.commons.impl.daemon;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.lang.Assertion;

import java.util.Map;

/**
 * Daemon's info.
 *
 * @author TINGARGIOLA
 */
final class DaemonInfo {

	/** Name of the daemon. */
	private final String name;
	private final int periodInSeconds;
	private final Class<? extends Daemon> daemonClass;
	private final Map<String, Object> daemonParams;

	/**
	 * Constructor.
	 *
	 * @param name Name of the daemon
	 * @param daemonClass Class.
	 * @param periodInSeconds daemon execution period.
	 * @param daemonParams Daemon params
	 */
	DaemonInfo(final String name, final Class<? extends Daemon> daemonClass, final int periodInSeconds, final Map<String, Object> daemonParams) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(daemonClass);
		Assertion.checkArgument(periodInSeconds > 0, "period {0} must be > 0", periodInSeconds);
		// -----
		this.name = name;
		this.daemonClass = daemonClass;
		this.periodInSeconds = periodInSeconds;
		this.daemonParams = daemonParams;
	}

	String getName() {
		return name;
	}

	/**
	 * Give the value of daemonClass.
	 *
	 * @return DaemonClass.
	 */
	Class<? extends Daemon> getDaemonClass() {
		return daemonClass;
	}

	/**
	 * Give the value of periodInSeconds.
	 *
	 * @return PeriodInSeconds.
	 */
	int getPeriodInSeconds() {
		return periodInSeconds;
	}

	/**
	 * Give the daemon params.
	 * @return Daemon params.
	 */
	Map<String, Object> getDaemonParams() {
		return daemonParams;
	}
}
