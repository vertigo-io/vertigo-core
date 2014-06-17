package io.vertigo.studio.impl.reporting.renderer;

import io.vertigo.studio.reporting.Report;

/**
 * Interface de rendu d'un rapport d'analyse relatif à une collection d'objets.
 * 
 * @author pchretien
 */
public interface ReportRenderer {
	/**
	 * Rendu d'un rapport (sous format HTML)
	 * @param report Rapport
	 */
	void render(final Report report);
}
