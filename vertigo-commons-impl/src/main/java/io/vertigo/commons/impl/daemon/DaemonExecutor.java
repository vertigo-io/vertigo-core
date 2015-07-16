package io.vertigo.commons.impl.daemon;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonDefinition;
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
	private final List<MyTimerTask> myTimerTasks = new ArrayList<>();

	/**
	* Enregistre un démon. 
	* Il sera lancé après le temp delay (en milliseconde) et sera réexécuté périodiquement toutes les period (en milliseconde).
	*
	* @param daemonDefinition Daemono's definition (DMN_XXX)
	* @param daemon Daemon to schedule.
	*/
	void scheduleDaemon(final DaemonDefinition daemonDefinition, final Daemon daemon) {
		Assertion.checkNotNull(daemonDefinition);
		Assertion.checkState(isActive, "Manager must be active to schedule a daemon");
		// -----
		MyTimerTask timerTask = new MyTimerTask(daemonDefinition, daemon);
		myTimerTasks.add(timerTask);
		scheduler.scheduleWithFixedDelay(timerTask, daemonDefinition.getPeriodInSeconds(), daemonDefinition.getPeriodInSeconds(), TimeUnit.SECONDS);
	}

	List<DaemonStat> getSats() {
		ListBuilder<DaemonStat> listBuilder = new ListBuilder<>();
		for (MyTimerTask timerTask : myTimerTasks) {
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

	private static class MyTimerTask implements Runnable {

		private static final Logger LOG = Logger.getLogger(MyTimerTask.class);
		private final Daemon daemon;
		private final DaemonDefinition daemonDefinition;
		//-----
		private long successes;
		private long failures;
		private DaemonStat.Status status = DaemonStat.Status.pending;

		MyTimerTask(final DaemonDefinition daemonDefinition, final Daemon daemon) {
			Assertion.checkNotNull(daemonDefinition);
			Assertion.checkNotNull(daemon);
			// -----
			this.daemon = daemon;
			this.daemonDefinition = daemonDefinition;
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

		private synchronized DaemonStat getStat() {
			//On copie les données 
			return new DaemonStatImpl(daemonDefinition, successes, failures, status);
		}

		private synchronized void onStart() {
			status = DaemonStat.Status.running;
			LOG.info("Start daemon: " + daemonDefinition.getName());
		}

		private synchronized void onFailure(Exception e) {
			status = DaemonStat.Status.pending;
			failures++;
			LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonDefinition.getName(), e);
		}

		private synchronized void onSuccess() {
			status = DaemonStat.Status.pending;
			successes++;
			LOG.info("Executio succeeded on daemon: " + daemonDefinition.getName());
		}
	}
}
