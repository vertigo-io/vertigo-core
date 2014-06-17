package io.vertigo.commons.analytics;

import io.vertigo.kernel.component.Manager;

/**
 * Accès centralisé à toutes les fonctions Analytiques.
 * 
 * @author pchretien
 */
public interface AnalyticsManager extends Manager {
	/**
	 * @return Agent de collecte
	 */
	AnalyticsAgent getAgent();
}
