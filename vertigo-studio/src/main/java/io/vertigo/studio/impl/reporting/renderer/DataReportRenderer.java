package io.vertigo.studio.impl.reporting.renderer;

import io.vertigo.studio.reporting.DataReport;

/**
 * Interface de rendu d'un rapport relatif à une seul objet.
 * 
 * @author tchassagnette
 */
public interface DataReportRenderer {

	/**
	 * Rendu d'un rapport (sous format HTML)
	 * @param dataReport Rapport
	 */
	void render(final DataReport dataReport);
}
