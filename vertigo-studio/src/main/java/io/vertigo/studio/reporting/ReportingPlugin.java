package io.vertigo.studio.reporting;

import io.vertigo.kernel.component.Plugin;

/**
 * Plugin permettant de créer un rapport particulier.
 *  
 * @author pchretien
 */
public interface ReportingPlugin extends Plugin {
	/**
	 * @return Rapport d'analyse d'un plugin
	 */
	Report analyze();
}
