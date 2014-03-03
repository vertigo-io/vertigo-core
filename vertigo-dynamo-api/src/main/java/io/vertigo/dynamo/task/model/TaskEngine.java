package io.vertigo.dynamo.task.model;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.work.WorkEngine;
import io.vertigo.kernel.lang.Assertion;

/**
 * Moteur précisant le mode d'exécution d'une définition de tache.
 * Attention ce moteur est avec état ; il est donc nécessaire de le recréer avant chaque utilisation.
 *
 * @author fconstantin, pchretien
 * @version $Id: TaskEngine.java,v 1.4 2014/01/20 18:57:19 pchretien Exp $
 * @see io.vertigo.dynamo.task.model.Task
 */
public abstract class TaskEngine implements WorkEngine<TaskResult, Task> {
	private Task input;
	private TaskResultBuilder output;

	/**
	 * Réalise l'exécution d'une tache.
	 * L'implémentation n'est pas responsable de la gestion de la transaction.
	 * Un rollback de la transaction sera automatiquement exécuté au cas où
	 * une exception survient.
	 * La tache permet d'accéder à la définition des paramètres d'entrée-sortie
	 * ainsi qu'à la chaine de configuration de la tache.
	 */
	protected abstract void execute();

	/** {@inheritDoc} */
	public final TaskResult process(final Task task) {
		Assertion.checkNotNull(task);
		//-----------------------------------------------------------------------------------
		input = task;
		output = new TaskResultBuilder(task.getDefinition());
		// les implémentations de TaskEngine utilisent setValue qui remplit le result (ouput).
		execute();
		return output.build();
	}

	/**
	* Getter avec un type générique.
	* Retourne la valeur d'un paramètre (INPUT)
	*
	* @param <J> Type java de l'objet recherché
	* @param attributeName Nom du paramètre
	* @return Valeur
	*/
	protected final <J> J getValue(final String attributeName) {
		return input.<J> getValue(attributeName);
	}

	/**
	 * Setter générique
	 * Affecte la valeur d'un paramètre (OUTPUT)
	 *
	 * @param attributeName Nom du paramètre
	 * @param o Valeur
	 */
	protected final void setValue(final String attributeName, final Object o) {
		output.withValue(attributeName, o);
	}

	/**
	 * Retourne la définition de la tache.
	 * taskDataSet est non visible (Framework).
	 *
	 * @return Définition de la tache
	 */
	protected final TaskDefinition getTaskDefinition() {
		return input.getDefinition();
	}

}
