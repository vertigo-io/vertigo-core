package io.vertigo.commons.impl.daemon;

import org.apache.log4j.Logger;

import io.vertigo.commons.daemon.DaemonStat;
import io.vertigo.lang.Assertion;

final class DaemonListener {
	private static final Logger LOG = Logger.getLogger(DaemonTimerTask.class);

	private long successes;
	private boolean lastExecSucceed;
	private long failures;
	private DaemonStat.Status status = DaemonStat.Status.pending;
	private final DaemonInfo daemonInfo;
	private final boolean verbose;

	public DaemonListener(final DaemonInfo daemonInfo, final boolean verbose) {
		Assertion.checkNotNull(daemonInfo);
		//---
		this.daemonInfo = daemonInfo;
		this.verbose = verbose;
	}

	//Les synchronized sont placés de façon à garantir l'intégrité des données
	//On effectue une copie/snapshot des stats de façon à ne pas perturber la suite du fonctionnement.

	synchronized DaemonStat getStat() {
		//On copie les données
		return new DaemonStatImpl(daemonInfo, successes, failures, status, lastExecSucceed);
	}

	synchronized void onStart() {
		status = DaemonStat.Status.running;
		if (verbose) {
			LOG.info("Start daemon: " + daemonInfo.getName());
		}
	}

	synchronized void onFailure(final Exception e) {
		status = DaemonStat.Status.pending;
		failures++;
		lastExecSucceed = false;
		LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonInfo.getName(), e);
	}

	synchronized void onSuccess() {
		status = DaemonStat.Status.pending;
		successes++;
		lastExecSucceed = true;
		if (verbose) {
			LOG.info("Execution succeeded on daemon: " + daemonInfo.getName());
		}
	}
}
