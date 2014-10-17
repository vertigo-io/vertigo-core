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

import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Résultat de l'exécution d'une tache.
 * @author dchallas
 */
public final class TaskResult {
	/**
	 * Map conservant les paramètres d'entrée et de sortie de la tache.
	 */
	private final Map<TaskAttribute, Object> taskAttributes;
	/**
	 * Définition de la tache.
	 */
	private final TaskDefinition taskDefinition;

	/**
	 * Constructeur.
	 * Le constructeur est protégé, il est nécessaire de passer par le Builder.
	 *
	 * @param dataSet Données de la tache.
	 */
	TaskResult(final TaskDefinition taskDefinition, final Map<TaskAttribute, Object> taskAttributes) {
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(taskAttributes);
		//----------------------------------------------------------------------
		this.taskDefinition = taskDefinition;
		for (final Entry<TaskAttribute, Object> entry : taskAttributes.entrySet()) {
			Assertion.checkArgument(!entry.getKey().isIn(), "only 'out' taskAttributes are allowed");
		}
		//---
		this.taskAttributes = Collections.unmodifiableMap(new HashMap<>(taskAttributes));
		checkValues();
	}

	private void checkValues() {
		for (final TaskAttribute taskAttribute : taskDefinition.getAttributes()) {
			if (!taskAttribute.isIn()) {
				//on ne prend que les attributes correspondant au mode output.
				//We check all attributes
				final Object value = this.taskAttributes.get(taskAttribute);
				taskAttribute.checkAttribute(value);
			}
		}
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
		// on préfère centraliser le cast ici plutot que dans les classes générées.
		final TaskAttribute taskAttribute = taskDefinition.getAttribute(attributeName);
		Assertion.checkArgument(!taskAttribute.isIn(), "only 'out' taskAttributes are allowed");
		return (V) taskAttributes.get(taskAttribute);
	}
}
