package io.vertigo.dynamo.task.model;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

/**
 * Résultat de l'execution du Task.
 * @author dchallas
 * @version $Id: TaskResultBuilder.java,v 1.3 2013/10/22 12:31:13 pchretien Exp $
 */
final class TaskResultBuilder implements Builder<TaskResult> {
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
	 */
	TaskResultBuilder(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//----------------------------------------------------------------------
		//Création du conteneur des paramètres du service
		dataSet = new TaskDataSet(taskDefinition, false);
	}

	/**
	 * Setter générique.
	 * Affecte la valeur d'un paramètre.
	 *
	 * @param attributeName Nom du paramètre
	 * @param o Valeur
	 */
	TaskResultBuilder withValue(final String attributeName, final Object o) {
		dataSet.setValue(attributeName, o);
		return this;
	}

	@Override
	public TaskResult build() {
		dataSet.makeUnmodifiable();
		return new TaskResult(dataSet);
	}

}
