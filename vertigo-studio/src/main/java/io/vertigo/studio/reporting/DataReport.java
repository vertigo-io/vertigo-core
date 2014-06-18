package io.vertigo.studio.reporting;

import java.util.List;

/**
 * Résultat d'une ligne d'analyse.
 * 
 * @author tchassagnette
 */
public interface DataReport {
	/**
	 * @return Intitulé de l'analyse effectuée. (ex : Nom de l'objet) 
	 */
	String getTitle();

	/**
	 * @return Nom du fichier
	 */
	String getFileName();

	/**
	 * Description HTML de la donéne
	 */
	String getHtmlDescription();

	/**
	 * @return Métriques associées. 
	 */
	List<Metric> getMetrics();
}
