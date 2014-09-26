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

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Résultat de l'execution du Task.
 * @author dchallas
 */
final class TaskResultBuilder implements Builder<TaskResult> {
	private final Map<String, Object> params = new HashMap<>();
	private final TaskDefinition taskDefinition;


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
		this.taskDefinition = taskDefinition;
	}

	/**
	 * Setter générique.
	 * Affecte la valeur d'un paramètre.
	 *
	 * @param attributeName Nom du paramètre
	 * @param o Valeur
	 */
	TaskResultBuilder withValue(final String attributeName, final Object value) {
		params.put(attributeName, value);
		return this;
	}

	@Override
	public TaskResult build() {
		return new TaskResult(taskDefinition, params);
	}

}
