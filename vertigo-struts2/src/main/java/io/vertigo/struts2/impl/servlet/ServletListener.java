package io.vertigo.struts2.impl.servlet;

import org.apache.log4j.Logger;

/**
 * Implémentation du listener des �v�nements produits par la servlet.
 * 
 * @author pchretien
 */
final class ServletListener {

	/**
	 * M�canisme de log racine
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
	 * Evénement remont� lors du démarrage de la servlet.
	 * @param servletName Nom de la servlet
	 */
	public void onServletStart(final String servletName) {
		if (generalLog.isInfoEnabled()) {
			generalLog.info("Start servlet " + servletName);
		}
	}

	/**
	 * Evénement remont� lors de l'arrêt de la servlet.
	 * @param servletName Nom de la servlet
	 */
	public void onServletDestroy(final String servletName) {
		if (generalLog.isInfoEnabled()) {
			generalLog.info("Destroy servlet " + servletName);
		}
	}
}
