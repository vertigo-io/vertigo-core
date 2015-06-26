package io.vertigo.commons.plugins.daemon.executor;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.impl.daemon.DaemonPlugin;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Implémentation basic du plugin de gestion de démon.
 *
 * @author TINGARGIOLA
 */
public final class ExecutorDaemonPlugin implements DaemonPlugin, Activeable {
	private boolean isActive;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

	/** {@inheritDoc} */
	@Override
	public void scheduleDaemon(final String daemonName, final Daemon daemon, final long periodInSeconds) {
		Assertion.checkNotNull(daemon);
		Assertion.checkState(isActive, "Le manager n'est pas actif.");
		// -----
		scheduler.scheduleWithFixedDelay(new MyTimerTask(daemonName, daemon), periodInSeconds, periodInSeconds, TimeUnit.SECONDS);
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

	private static class MyTimerTask implements Runnable {

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
			try {//try catch needed to ensure execution aren't suppressed
				LOG.info("Start daemon: " + daemonName);
				daemon.run();
				LOG.info("Executio succeeded on daemon: " + daemonName);
			} catch (final Exception e) {
				LOG.error("Daemon :  an error has occured during the execution of the daemon: " + daemonName, e);
			}
		}
	}
}
