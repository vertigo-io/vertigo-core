package io.vertigo.studio.impl.reporting.renderer;

import io.vertigo.studio.reporting.Report;

/**
 * Interface de rendu d'un rapport d'analyse relatif à une collection d'objets.
 * 
 * @author pchretien
 * @version $Id: ReportRenderer.java,v 1.1 2013/07/11 10:04:05 npiedeloup Exp $
 */
public interface ReportRenderer {
	/**
	 * Rendu d'un rapport (sous format HTML)
	 * @param report Rapport
	 */
	void render(final Report report);
}
