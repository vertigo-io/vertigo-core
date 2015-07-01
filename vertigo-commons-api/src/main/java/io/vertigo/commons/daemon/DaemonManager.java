package io.vertigo.commons.daemon;

import io.vertigo.lang.Component;

/**
 * Manages daemons.
 * A daemon is technical (vs job or batch).
 *
 * @author tingargiola
 */
public interface DaemonManager extends Component {

	/**
	 * Démarre l'ensemble des démons préalablement enregistré dans le spaceDefinition.
	 */
	void startAllDaemons();
	// void stopAllDaemons();
}
