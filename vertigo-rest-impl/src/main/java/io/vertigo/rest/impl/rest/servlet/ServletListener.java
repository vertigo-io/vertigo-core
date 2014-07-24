package io.vertigo.rest.impl.rest.servlet;

import org.apache.log4j.Logger;

/**
 * Implémentation du listener des événements produits par la servlet.
 * @author pchretien
 */
final class ServletListener {

	/**
	 * Mécanisme de log racine
	 */
	private final Logger generalLog;

	/**
	 * Constructeur.
	 */
	ServletListener() {
		generalLog = Logger.getRootLogger();
	}

	// --------------------------------------------------------------------------

	/**
	 * Evénement remonté lors du démarrage de la servlet.
	 * @param servletName Nom de la servlet
	 */
	public void onServletStart(final String servletName) {
		if (generalLog.isInfoEnabled()) {
			generalLog.info("Start servlet " + servletName);
		}
	}

	/**
	 * Evénement remonté lors de l'arrêt de la servlet.
	 * @param servletName Nom de la servlet
	 */
	public void onServletDestroy(final String servletName) {
		if (generalLog.isInfoEnabled()) {
			generalLog.info("Destroy servlet " + servletName);
		}
	}
}
