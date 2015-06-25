package io.vertigo.commons.plugins.daemon.timer;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.impl.daemon.DaemonPlugin;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Implémentation basic du plugin de gestion de démon.
 *
 * @author TINGARGIOLA
 */
public final class TimerDaemonPlugin implements DaemonPlugin, Activeable {
	private boolean isActive;
	private final Timer timer = new Timer(true);

	/** {@inheritDoc} */
	@Override
	public void scheduleDaemon(final String daemonName, final Daemon daemon, final long periodInSeconds) {
		Assertion.checkNotNull(daemon);
		Assertion.checkState(isActive, "Le manager n'est pas actif.");
		// -----
		final long delay = 0; // starts now
		timer.schedule(new MyTimerTask(daemonName, daemon), delay, periodInSeconds * 1000);
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		isActive = true;
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		isActive = false;
	}

	private static class MyTimerTask extends TimerTask {

		private static final Logger LOG = Logger.getLogger(MyTimerTask.class);
		private final Daemon daemon;
		private final String daemonName;

		MyTimerTask(final String daemonName, final Daemon daemon) {
			Assertion.checkArgNotEmpty(daemonName);
			Assertion.checkNotNull(daemon);
			// -----
			this.daemon = daemon;
			this.daemonName = daemonName;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			try {
				LOG.info("Start daemon: " + daemonName);
				daemon.run();
				LOG.info("Executio succeeded on daemon: " + daemonName);
			} catch (final Exception e) {
				LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonName, e);
			}
		}
	}
}
