package io.vertigo.dynamo.task.model;

import io.vertigo.kernel.lang.Assertion;

/**
 * Résultat de l'exécution d'une tache.
 * @author dchallas
 */
public final class TaskResult {
	/**
	 * Conteneur des données et de l'état du service
	 */
	private final TaskDataSet dataSet;

	/**
	 * Constructeur. 
	 * Le constructeur est protégé, il est nécessaire de passer par le Builder.
	 *
	 * @param dataSet Données de la tache.
	 */
	TaskResult(final TaskDataSet dataSet) {
		Assertion.checkNotNull(dataSet);
		Assertion.checkArgument(!dataSet.isModifiable(), "dataset must be immutable");
		//----------------------------------------------------------------------
		this.dataSet = dataSet;
	}

	/**
	 * Getter générique.
	 * Retourne la valeur d'un paramètre conforme au contrat de l'attribut du service
	 *
	 * @param attributeName Nom du paramètre
	 * @param <V> Type de la valeur
	 * @return Valeur
	 */
	public <V> V getValue(final String attributeName) {
		return dataSet.<V> getValue(attributeName);
	}
}
