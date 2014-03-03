package io.vertigo.dynamo.task.model;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

/**
 * Builder permettant de créer une tache.
 * @author  pchretien
 * @version $Id: TaskBuilder.java,v 1.3 2013/10/22 12:31:13 pchretien Exp $
 */
public final class TaskBuilder implements Builder<Task> {
	/**
	 * Conteneur des données et de l'état du service
	 */
	private final TaskDataSet dataSet;

	/**
	 * Initialise la tache.
	 * Le constructeur est invoqué par la Factory.
	 * Cette méthode ne doit pas être appelée directement.
	 *
	 * @param taskDefinition Définition de la tache
	 * param workListener Listener enregistrant les événements produits par l'exécution  des taches
	 */
	public TaskBuilder(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//----------------------------------------------------------------------
		//Création du conteneur des paramètres du service
		dataSet = new TaskDataSet(taskDefinition, true);
	}

	/**
	 * Affecte la valeur d'un paramètre.
	 *
	 * @param attributeName Nom du paramètre
	 * @param o Valeur
	 */
	public TaskBuilder withValue(final String attributeName, final Object o) {
		dataSet.setValue(attributeName, o);
		return this;
	}

	/** {@inheritDoc} */
	public Task build() {
		dataSet.makeUnmodifiable();
		return new Task(dataSet);
	}
}
