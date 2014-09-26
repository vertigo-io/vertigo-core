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
import io.vertigo.dynamo.domain.metamodel.ConstraintException;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Conteneur de données permettant l'exécution d'une tache.
 * Ce conteneur est non-serializable car les services ont un cycle de vie très court
 * et ne sont pas résistant à la migration de JVM par sérialisation/désérialisation.
 * 
 * Il y a un dataset pour les données en entrée et un pour les données en sortie.
 * Chaque DataSet est non modifiable.
 * 
 *
 * @author  pchretien
 */
final class TaskDataSet {
	/**
	 * Définition de la tache.
	 */
	private final TaskDefinition taskDefinition;
	/**
	 * Map conservant les paramètres d'entrée et de sortie de la tache.
	 */
	private final Map<TaskAttribute, Object> taskAttributes = new HashMap<>();

	private final boolean in;

	/**
	 * Constructeur
	 * @param taskDefinition Définition de la tache dont on représente le conteneur de données
	 */
	TaskDataSet(final TaskDefinition taskDefinition, final boolean in, final Map<String, Object> params) {
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(params);
		//----------------------------------------------------------------------
		this.taskDefinition = taskDefinition;
		this.in = in;
		//---
		for (final Entry<String, Object> entry : params.entrySet()){
			final TaskAttribute taskAttribute = getTaskDefinition().getAttribute(entry.getKey());
			Assertion.checkArgument(taskAttribute.isIn()^!in, "only taskAttributes where  in='{0}' are allowed", in);
			taskAttributes.put(taskAttribute, entry.getValue());
		}
		//---
		for (final TaskAttribute taskAttribute : taskDefinition.getAttributes()){
			if (taskAttribute.isIn()^!in){ //on ne prend que les attributes correspondant au mode.
				final Object value = taskAttributes.get(taskAttribute);
				checkAttribute(taskAttribute, value);
			}
		}
	}


	/**
	 * Vérifie la cohérence des arguments d'un Attribute
	 * Vérifie que l'objet est cohérent avec le type défini sur l'attribut.
	 * @param attributeName Nom de l'attribut de tache
	 * @param value Object primitif ou DtObject ou bien DtList
	 */
	private static void checkAttribute(final TaskAttribute taskAttribute, final Object value) {
		Assertion.checkNotNull(taskAttribute);
		//---------------------------------------------------------------------
		if (taskAttribute.isNotNull()) {
			Assertion.checkNotNull(value, "Attribut task {0} ne doit pas etre null (cf. paramétrage task)", taskAttribute.getName());
		}
		try {
			taskAttribute.getDomain().checkValue(value);
		} catch (final ConstraintException e) {
			//On retransforme en Runtime pour conserver une API sur les getters et setters.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Getter générique
	 * Retourne la valeur d'un paramètre conforme au contrat de l'attribut du service
	 *
	 * @param attributeName Nom du paramètre
	 * @return Valeur
	 */
	<V> V getValue(final String attributeName) {
		// on préfère centraliser le cast ici plutot que dans les classes générées.
		//L'idée est d'interdire de lire une valeur d'un dataSet alors que celui-ci n'est pas encore totalement initialisé.
		//---------------------------------------------------------------------
		final TaskAttribute taskAttribute= getTaskDefinition().getAttribute(attributeName);
		Assertion.checkArgument(taskAttribute.isIn()^!in, "only taskAttributes where  in='{0}' are allowed", in);
		return (V) taskAttributes.get(taskAttribute);
	}

	/**
	 * @return Définition de la tache
	 */
	TaskDefinition getTaskDefinition() {
		return taskDefinition;
	}

	/** {@inheritDoc}*/
	@Override
	public String toString() {
		return taskAttributes.toString();
	}
}
