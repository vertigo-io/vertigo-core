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
package io.vertigo.dynamo.store.datastore;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;

/**
 * Defines the way to acces and store all the data.
 * Les méthodes de mises à jour lacent des erreurs utilisateurs et techniques.
 * Les méthodes d'accès aux données ne lancent que des erreurs techniques.
 *
 * @author  pchretien
 */
public interface DataStore {
	/**
	 * Nombre d'éléments présents dans le sysème de persistance.
	 * @param dtDefinition Définition de DT
	 * @return Nombre d'éléments.
	 */
	int count(final DtDefinition dtDefinition);

	/**
	 * Récupération d'un objet persistant par son URI.
	 * Lorsque l'objet est en lecture seule il est possible d'accéder au objets partagés. (Liste de référence paér ex)
	 * L'objet doit exister.
	 *
	 * @param <D> Type de l'objet
	 * @param uri Uri de l'object
	 * @return object récupéré NOT NULL
	 */
	<D extends DtObject> D get(final URI<D> uri);

	/**
	 * Récupération d'une liste identifiée par son URI.
	 *
	 * @param <D> Type des objets de la collection
	 * @param uri URI de la collection à récupérer
	 * @return DtList DTC
	 */
	<D extends DtObject> DtList<D> readAll(final DtListURI uri);

	/**
	 * Loads and marks element for update, and ensure non concurrency.
	 * @param <D> Object type
	 * @param uri URI of object
	 * @return object to update
	 */
	<D extends DtObject> D readForUpdate(URI<? extends DtObject> uri);

	/**
	* Create an object.
	* No object with the same id must have been created previously.
	*
	* @param dto Object to create
	*/
	void create(DtObject dto);

	/**
	* Update an object.
	* This object must have an id.
	* @param dto Object to update
	*/
	void update(DtObject dto);

	/**
	* Merge an object.
	* Strategy to create or update this object depends on the state of the database.
	*
	*  - If  this object is already created : update
	*  - If  this object is not found : create
	*
	* @param dto Object to merge
	*/
	void merge(DtObject dto);

	/**
	 * Destruction d'un objet persistant par son URI.
	 *
	 * @param uri URI de l'objet à supprimer
	 */
	void delete(URI<? extends DtObject> uri);

}
