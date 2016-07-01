/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import org.apache.log4j.Logger;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonStat;
import io.vertigo.lang.Assertion;

/**
 * @author TINGARGIOLA
 */
final class DaemonTimerTask implements Runnable {
	private static final Logger LOG = Logger.getLogger(DaemonTimerTask.class);
	private final Daemon daemon;
	private final DaemonInfo daemonInfo;
	//-----
	private long successes;
	private boolean lastExecSucceed;
	private long failures;
	private DaemonStat.Status status = DaemonStat.Status.pending;

	DaemonTimerTask(final DaemonInfo daemonInfo, final Daemon daemon) {
		Assertion.checkNotNull(daemonInfo);
		Assertion.checkNotNull(daemon);
		// -----
		this.daemon = daemon;
		this.daemonInfo = daemonInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		try {//try catch needed to ensure execution aren't suppressed
			onStart();
			daemon.run();
			onSuccess();
		} catch (final Exception e) {
			onFailure(e);
		}
	}

	//Les synchronized sont placés de façon à garantir l'intégrité des données
	//On effectue une copie/snapshot des stats de façon à ne pas perturber la suite du fonctionnement.

	synchronized DaemonStat getStat() {
		//On copie les données
		return new DaemonStatImpl(daemonInfo, successes, failures, status, lastExecSucceed);
	}

	private synchronized void onStart() {
		status = DaemonStat.Status.running;
		LOG.info("Start daemon: " + daemonInfo.getName());
	}

	private synchronized void onFailure(final Exception e) {
		status = DaemonStat.Status.pending;
		failures++;
		lastExecSucceed = false;
		LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonInfo.getName(), e);
	}

	private synchronized void onSuccess() {
		status = DaemonStat.Status.pending;
		successes++;
		lastExecSucceed = true;
		LOG.info("Executio succeeded on daemon: " + daemonInfo.getName());
	}
}
