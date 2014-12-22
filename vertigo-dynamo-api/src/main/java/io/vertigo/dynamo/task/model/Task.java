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
 * Gestion des taches.
 *
 * Les taches sont implémentés par les classes dérivées de {@link io.vertigo.dynamo.task.model.TaskEngine}
 *
 * Une tache peut être perçue comme une instance d'une {@link io.vertigo.dynamo.task.metamodel.TaskDefinition} ;
 * celle-ci doit être préalablement déclarée.
 *
 * L'utilisation d'une tache se fait en 4 étapes :
 * -  Etape 1 : récupération de la tache.
 * -  Etape 2 : définition des attributs (ou paramètres) d'entrées. <code>srv.setXXX(...);</code>
 * -  Etape 3 : exécution de la tache <code>srv.execute();</code>
 * -  Etape 4 : récupération des paramètres de sorties. <code>srv.getXXX(...);</code>
 *
 * Notes :
 * -  Une tache s'exécute dans le cadre de la transaction courante.
 * -  Une tache n'est pas sérializable ; elle doit en effet posséder une durée de vie la plus courte possible.
 * @author  fconstantin, pchretien
 */
public final class Task {
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
	 */
	Task(final TaskDefinition taskDefinition, final Map<TaskAttribute, Object> taskAttributes) {
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(taskAttributes);
		//-----
		this.taskDefinition = taskDefinition;
		for (final Entry<TaskAttribute, Object> entry : taskAttributes.entrySet()) {
			Assertion.checkArgument(entry.getKey().isIn(), "only 'in' taskAttributes are allowed");
		}
		//---
		this.taskAttributes = Collections.unmodifiableMap(new HashMap<>(taskAttributes));
		checkValues();
	}

	private void checkValues() {
		for (final TaskAttribute taskAttribute : taskDefinition.getAttributes()) {
			if (taskAttribute.isIn()) {
				//on ne prend que les attributes correspondant au mode.
				//We check all attributes
				final Object value = taskAttributes.get(taskAttribute);
				taskAttribute.checkAttribute(value);
			}
		}
	}

	/**
	 * Getter générique.
	 * Retourne la valeur d'un paramètre conforme au contrat de l'attribut du service.
	 *
	 * @param attributeName Nom du paramètre
	 * @param <V> Type de la valeur
	 * @return Valeur
	 */
	public <V> V getValue(final String attributeName) {
		// on préfère centraliser le cast ici plutot que dans les classes générées.
		final TaskAttribute taskAttribute = taskDefinition.getAttribute(attributeName);
		Assertion.checkArgument(taskAttribute.isIn(), "only 'in' taskAttributes are allowed");
		return (V) taskAttributes.get(taskAttribute);
	}

	/**
	 * @return Définition de la task.
	 */
	public TaskDefinition getDefinition() {
		return taskDefinition;
	}
}
