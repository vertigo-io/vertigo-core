package io.vertigo.commons.impl.daemon;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.lang.Plugin;

/**
 * Plugin de gestion des démons.
 *
 * @author TINGARGIOLA
 */
public interface DaemonPlugin extends Plugin {

	/**
	 * Enregistre un démon. Il sera lancé après le temp delay (en milliseconde) et sera réexécuté périodiquement toute
	 * les period (en milliseconde).
	 *
	 * @param daemonName Nom du démon (DMN_XXX)
	 * @param daemon Le démon à lancer.
	 * @param periodInSeconds La période d'exécution du démon.
	 */
	void scheduleDaemon(String daemonName, Daemon daemon, long periodInSeconds);
}
