package io.vertigo.commons.impl.daemon;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonDefinition;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.core.Home;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.lang.Assertion;

import javax.inject.Inject;

/**
 * Manager de gestion du deamon.
 *
 * @author TINGARGIOLA
 */
public final class DaemonManagerImpl implements DaemonManager {

	private final DaemonPlugin daemonPlugin;

	/**
	 * Construct an instance of DeamonManagerImpl.
	 *
	 * @param daemonPlugin Plugin de gestion du deamon.
	 */
	@Inject
	public DaemonManagerImpl(final DaemonPlugin daemonPlugin) {
		Assertion.checkNotNull(daemonPlugin);
		// -----
		this.daemonPlugin = daemonPlugin;
	}

	/**
	 * Démarre un démon.
	 * Celui-ci aura été préalablement enregistré.
	 * Il sera lancé puis réexécuté périodiquement.
	 * L'instance du démon est créée par injection de dépendances.
	 *
	 * @param daemonDefinition Le démon à lancer.
	 */
	private void startDaemon(final DaemonDefinition daemonDefinition) {
		Assertion.checkNotNull(daemonDefinition);
		// -----
		final Daemon daemon = createDaemon(daemonDefinition);
		daemonPlugin.scheduleDaemon(daemonDefinition.getName(), daemon, daemonDefinition.getPeriodInSeconds());
	}

	/**
	 * @param daemonDefinition
	 * @return Dameon
	 */
	private static Daemon createDaemon(final DaemonDefinition daemonDefinition) {
		return Injector.newInstance(daemonDefinition.getDaemonClass(), Home.getComponentSpace());
	}

	/** {@inheritDoc} */
	@Override
	public void startAllDaemons() {
		for (final DaemonDefinition daemonDefinition : Home.getDefinitionSpace().getAll(DaemonDefinition.class)) {
			startDaemon(daemonDefinition);
		}
	}
}
