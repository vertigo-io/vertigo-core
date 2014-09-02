/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.task.model;

import io.vertigo.core.lang.Builder;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;

/**
 * Builder permettant de créer une tache.
 * @author  pchretien
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
