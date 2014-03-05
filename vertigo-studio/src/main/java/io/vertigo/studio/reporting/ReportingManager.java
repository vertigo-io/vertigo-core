package io.vertigo.studio.reporting;

import io.vertigo.kernel.component.Manager;

import java.util.List;

/**
 * Manager de création de rapport d'exécution sur les taches (dao, pao).
 * 
 * @author tchassagnette
 * @version $Id: ReportingManager.java,v 1.2 2013/10/22 10:59:19 pchretien Exp $
 */
public interface ReportingManager extends Manager {
	/**
	 * Méthode d'analyse des requetes.
	 */
	void analyze();

	/**
	 * @return Liste des plugins utilisés.
	 */
	List<ReportingPlugin> getReportingPlugins();
}
