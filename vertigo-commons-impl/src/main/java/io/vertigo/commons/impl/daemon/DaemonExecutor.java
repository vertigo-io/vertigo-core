package io.vertigo.commons.impl.daemon;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonStat;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Implémentation basic du plugin de gestion de démon.
 *
 * @author TINGARGIOLA
 */
final class DaemonExecutor implements Activeable {
	private boolean isActive;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private final List<DaemonTimerTask> myTimerTasks = new ArrayList<>();

	/**
	* Enregistre un démon.
	* Il sera lancé après le temp delay (en milliseconde) et sera réexécuté périodiquement toutes les period (en milliseconde).
	*
	* @param daemonInfo Daemono's info
	* @param daemon Daemon to schedule.
	*/
	void scheduleDaemon(final DaemonInfo daemonInfo, final Daemon daemon) {
		Assertion.checkNotNull(daemonInfo);
		Assertion.checkState(isActive, "Manager must be active to schedule a daemon");
		// -----
		final DaemonTimerTask timerTask = new DaemonTimerTask(daemonInfo, daemon);
		myTimerTasks.add(timerTask);
		scheduler.scheduleWithFixedDelay(timerTask, daemonInfo.getPeriodInSeconds(), daemonInfo.getPeriodInSeconds(), TimeUnit.SECONDS);
	}

	/**
	 * @return Daemons stats
	 */
	List<DaemonStat> getStats() {
		final ListBuilder<DaemonStat> listBuilder = new ListBuilder<>();
		for (final DaemonTimerTask timerTask : myTimerTasks) {
			listBuilder.add(timerTask.getStat());
		}
		return listBuilder.unmodifiable().build();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		isActive = true;
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		scheduler.shutdown();
		isActive = false;
	}

	private static final class DaemonTimerTask implements Runnable {

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
				this.onStart();
				daemon.run();
				this.onSuccess();
			} catch (final Exception e) {
				this.onFailure(e);
			}
		}

		//Les synchronized sont placés de façon à garantir l'intégrité des données
		//On effectue une copie/snapshot des stats de façon à ne pas perturber la suite du fonctionnement.

		private synchronized DaemonStat getStat() {
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
}
