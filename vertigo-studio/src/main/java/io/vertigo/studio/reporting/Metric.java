package io.vertigo.studio.reporting;

/**
 * Interface décrivant un résultat de metric.
 * 
 * @author tchassagnette, pchretien
 */
public interface Metric {
	enum Status {
		/** Exécution OK*/
		Executed,
		/** Erreur lors de l'exécution*/
		Error,
		/** Métrique non pertinente*/
		Rejected
	}

	/**
	 * @return Status de la métrique.
	 */
	Status getStatus();

	/**
	 * @return Titre de la métrique. (notNull)
	 */
	String getTitle();

	/**
	 * @return Unité de la métrique. (notNull)
	 */
	String getUnit();

	/**
	 * @return Valeur de la métrique. (Integer, Long, String, etc..) 
	 */
	Object getValue();

	/**
	 * @return Complément d'information sur la valeur. (nullable)
	 */
	String getValueInformation();
}
