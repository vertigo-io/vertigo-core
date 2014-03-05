package io.vertigo.studio.reporting;

import io.vertigo.kernel.component.Plugin;

/**
 * Plugin permettant de créer un rapport particulier.
 *  
 * @author pchretien
 * @version $Id: ReportingPlugin.java,v 1.2 2013/10/22 10:59:19 pchretien Exp $
 */
public interface ReportingPlugin extends Plugin {
	/**
	 * @return Rapport d'analyse d'un plugin
	 */
	Report analyze();
}
