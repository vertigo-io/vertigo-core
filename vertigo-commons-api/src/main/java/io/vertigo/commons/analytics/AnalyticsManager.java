package io.vertigo.commons.analytics;

import io.vertigo.kernel.component.Manager;

/**
 * Accès centralisé à toutes les fonctions Analytiques.
 * 
 * @author pchretien
 * @version $Id: AnalyticsManager.java,v 1.2 2013/10/22 10:42:32 pchretien Exp $
 */
public interface AnalyticsManager extends Manager {
	/**
	 * @return Agent de collecte
	 */
	AnalyticsAgent getAgent();
}
