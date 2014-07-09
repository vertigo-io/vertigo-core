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

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;

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
	 * Conteneur des données et de l'état du service
	 */
	private final TaskDataSet dataSet;

	/**
	 * Constructeur. 
	 * Le constructeur est protégé, il est nécessaire de passer par le Builder.
	 *
	 * @param dataSet Données de la tache.
	 */
	Task(final TaskDataSet dataSet) {
		Assertion.checkNotNull(dataSet);
		Assertion.checkArgument(!dataSet.isModifiable(), "dataset must be immutable");
		//----------------------------------------------------------------------
		this.dataSet = dataSet;
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
		return dataSet.<V> getValue(attributeName);
	}

	/**
	 * @return Définition de la task.
	 */
	public TaskDefinition getDefinition() {
		return dataSet.getTaskDefinition();
	}
}
