/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
/**
 *
 */
package io.vertigo.dynamo.impl.store.util;

import java.util.Collection;
import java.util.Map;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.Entity;

/**
 * Interface pour permettre des opérations avec des collections.
 *
 * @param <E> the type of entity
 * @param <P> Type de la clef primaire.
 * @author jmforhan
 */
public interface BrokerBatch<E extends Entity, P> {

	/**
	 * Récupère la liste des objets correspondant à des ids. On n'assure pas l'ordre de la liste par rapport à l'ordre des
	 * identifiants en entré. On peut avoir également une liste plus petites s'il y a des doublons dans la liste en entrée ou que
	 * des identifiants ne correspondant à rien en base.
	 * @param dtDefinition la dtDefinition
	 * @param idList liste des identifiants
	 * @return Liste des objets correspondants.
	 */
	DtList<E> getList(final DtDefinition dtDefinition, Collection<P> idList);

	/**
	 * Récupère la liste des objets correspondant à des ids et retourne sous forme de map entre la clé primaire et l'objet
	 * correspondant. Cette méthode est se base sur getList(). Il est possible qu'un id passé en entrée ne se retrouve pas comme
	 * clé de la map s'il n'y a aucun object associé à cet identifiant en base.
	 * @param dtDefinition la dtDefinition
	 * @param idList liste des identifiants
	 * @return map entre index et l'objet associé.
	 */
	Map<P, E> getMap(final DtDefinition dtDefinition, Collection<P> idList);

	/**
	 * Récupère la liste des objets associé à une collection de clé étrangère.
	 * @param dtDefinition la dtDefinition
	 * @param fieldName champ de sélection des objets à récupérer
	 * @param value collection des valeurs à utiliser pour sélectionner les objets
	 * @param <O> type de la valeur de sélection
	 * @return Liste des objets correspondants.
	 */
	<O> DtList<E> getListByField(final DtDefinition dtDefinition, final String fieldName, final Collection<O> value);

	/**
	 * Récupère la liste des objets associé à une collection de clé étrangère et la retourne sous forme de Map dont la clé est
	 * l'objet de sélection.
	 *
	 * @param dtDefinition la dtDefinition
	 * @param fieldName champ de sélection des objets à récupérer
	 * @param value collection des valeurs à utiliser pour sélectionner les objets
	 * @param <O> type de la valeur de sélection
	 * @return map entre valeur de sélection et objet associé.
	 */
	<O> Map<O, DtList<E>> getMapByField(final DtDefinition dtDefinition, final String fieldName, final Collection<O> value);
}
