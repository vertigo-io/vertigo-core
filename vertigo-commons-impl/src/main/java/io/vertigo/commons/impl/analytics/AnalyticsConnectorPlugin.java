package io.vertigo.commons.impl.analytics;

import io.vertigo.lang.Plugin;

/**
 * Connecteur des process.
 * Les messages sont composes des Processus et envoyes ; un consommateur les traitera.
 *
 * @author pchretien, npiedeloup
 * @version $Id: NetPlugin.java,v 1.1 2012/03/22 18:20:57 pchretien Exp $
 */
@FunctionalInterface
public interface AnalyticsConnectorPlugin extends Plugin {
	/**
	 * Adds a process to a connector which acts as a consumer.
	 * @param process the process
	 */
	void add(AProcess process);
}
