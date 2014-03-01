package io.vertigo.commons.analytics;

/**
 * Agent de collecte.
 * @author pchretien
 * @version $Id: AnalyticsAgent.java,v 1.2 2013/07/29 11:42:53 pchretien Exp $
 */
public interface AnalyticsAgent {
	/**
	 * Démarrage d'un processus.
	 * @param processType Type du processus
	 * @param processName Nom du processus
	 */
	void startProcess(final String processType, final String processName);

	/**
	 * Incrémente une mesure (set si pas présente).
	 * @param measureType Type de mesure
	 * @param value Incrément de la mesure
	 */
	void incMeasure(final String measureType, final double value);

	/**
	* Affecte une valeur fixe à la mesure.
	* A utiliser pour les exceptions par exemple (et toute donnée ne s'ajoutant pas). 
	* @param measureType Type de mesure
	* @param value valeur de la mesure
	*/
	void setMeasure(final String measureType, final double value);

	/**
	 * Affecte une valeur fixe à une meta-donnée.
	 *  
	 * @param metaDataName Nom de la meta-donnée
	 * @param value Valeur de la meta-donnée
	 */
	void addMetaData(final String metaDataName, final String value);

	/**
	 * Termine l'enregistrement du process.
	 */
	void stopProcess();
}
