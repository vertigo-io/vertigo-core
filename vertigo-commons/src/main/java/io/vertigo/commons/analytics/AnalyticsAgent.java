package io.vertigo.commons.analytics;

/**
 * Agent de collecte.
 * @author pchretien
 * @version $Id: AnalyticsAgent.java,v 1.2 2013/07/29 11:42:53 pchretien Exp $
 */
public interface AnalyticsAgent {
	/**
	 * D�marrage d'un processus.
	 * @param processType Type du processus
	 * @param processName Nom du processus
	 */
	void startProcess(final String processType, final String processName);

	/**
	 * Incr�mente une mesure (set si pas pr�sente).
	 * @param measureType Type de mesure
	 * @param value Incr�ment de la mesure
	 */
	void incMeasure(final String measureType, final double value);

	/**
	* Affecte une valeur fixe � la mesure.
	* A utiliser pour les exceptions par exemple (et toute donn�e ne s'ajoutant pas). 
	* @param measureType Type de mesure
	* @param value valeur de la mesure
	*/
	void setMeasure(final String measureType, final double value);

	/**
	 * Affecte une valeur fixe � une meta-donn�e.
	 *  
	 * @param metaDataName Nom de la meta-donn�e
	 * @param value Valeur de la meta-donn�e
	 */
	void addMetaData(final String metaDataName, final String value);

	/**
	 * Termine l'enregistrement du process.
	 */
	void stopProcess();
}
