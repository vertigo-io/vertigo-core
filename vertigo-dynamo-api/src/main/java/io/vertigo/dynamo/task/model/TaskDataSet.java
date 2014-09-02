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

import io.vertigo.core.lang.Modifiable;
import io.vertigo.dynamo.domain.metamodel.ConstraintException;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
final class TaskDataSet implements Modifiable {
	/**
	 * Définition de la tache. 
	 */
	private final TaskDefinition taskDefinition;
	/**
	 * Map conservant les paramètres d'entrée et de sortie de la tache.
	 */
	private final Map<String, Object> params = new HashMap<>();

	private boolean modifiable = true;

	private final boolean isIn;

	/**
	 * Constructeur
	 * @param taskDefinition Définition de la tache dont on représente le conteneur de données
	 */
	TaskDataSet(final TaskDefinition taskDefinition, final boolean isIn) {
		Assertion.checkNotNull(taskDefinition);
		//----------------------------------------------------------------------
		this.taskDefinition = taskDefinition;
		this.isIn = isIn;
	}

	/**
	* Permet de tester les paramètres null avant et après l'exécution de la requête.
	*/
	private void check() {
		//On vérifie que tous les champs in not null sont renseignés
		for (final TaskAttribute attribute : getTaskDefinition().getAttributes()) {
			// si before, on vérifie uniquement les paramétres en entrée
			// si after, on vérifie uniquement les paramétres en sortie
			if (isIn && attribute.isIn() || !isIn && !attribute.isIn()) {
				//  on vérifie que seuls les paramètres obligatoires sont non nuls
				final Object value = params.get(attribute.getName());
				if (attribute.isNotNull()) {
					Assertion.checkNotNull(value, "Attribut task {0} ne doit pas etre null (cf. paramétrage task)", attribute.getName());
				}
			}
		}
	}

	/**
	 * Vérifie la cohérence des arguments d'un Attribute
	 * Vérifie que l'objet est cohérent avec le type défini sur l'attribut.
	 * @param attributeName Nom de l'attribut de tache
	 * @param value Object primitif ou DtObject ou bien DtList
	 * @param getter Si on gette la donnée correspondant à l'attribut
	 */
	private void checkAttribute(final String attributeName, final Object value, final boolean getter) {
		Assertion.checkNotNull(attributeName);
		//---------------------------------------------------------------------
		//On récupère la définition de l'attribut de tache correspondant au nom
		final TaskAttribute attribut = getTaskDefinition().getAttribute(attributeName);

		try {
			attribut.getDomain().checkValue(value);
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
		Assertion.checkArgument(!modifiable, "Le dataSet est modifiable ; seuls les setters sont autorisés.");
		//L'idée est d'interdire de lire une valeur d'un dataSet alors que celui-ci n'est pas encore totalement initialisé.
		//---------------------------------------------------------------------
		final V value = (V) params.get(attributeName);
		checkAttribute(attributeName, value, true);
		return value;
	}

	/**
	 * Setter Générique
	 * Garantit que la valeur passée est conforme au contrat de l'attribut du service
	 *
	 * @param attributeName Nom du champ modifié.
	 * @param o Nouvelle valeur du champ.
	 */
	void setValue(final String attributeName, final Object o) {
		Assertion.checkArgument(modifiable, "Le dataSet n''est pas modifiable.");
		checkAttribute(attributeName, o, false);
		//---------------------------------------------------------------------
		params.put(attributeName, Serializable.class.cast(o));
	}

	/**
	 * @return ID de la Définition de la tache
	 */
	TaskDefinition getTaskDefinition() {
		return taskDefinition;
	}

	/** {@inheritDoc}*/
	@Override
	public String toString() {
		//Représentation textuelle des paramètres
		return params.toString();
	}

	/** {@inheritDoc} */
	public boolean isModifiable() {
		return modifiable;
	}

	/** {@inheritDoc} */
	public void makeUnmodifiable() {
		Assertion.checkArgument(modifiable, "Les données sont déjà non modifiables.");
		check();
		//----------------------------------------------------------------------
		modifiable = false;
	}
}
