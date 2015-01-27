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
package io.vertigo.dynamo.persistence.datastore;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;

/**
 * Objet permettant de gérer les accès aux systèmes de stockage.
 *
 * @author pchretien
 */
public interface DataStore {
	//==========================================================================
	//=============================== READ =====================================
	//==========================================================================

	/**
	 * Nombre d'éléments.
	 * @param dtDefinition Définition de DT
	 * @return Nombre d'éléments.
	 */
	int count(final DtDefinition dtDefinition);

	/**
	 * Récupération de l'objet correspondant à l'URI fournie.
	 * Peut-être null.
	 *
	 * @param uri URI de l'objet à charger
	 * @return D correspondant à l'URI fournie.
	 * @param <D> Type de l'objet
	 */
	<D extends DtObject> D load(URI<D> uri);

	/**
	 * Récupération d'une liste correspondant à l'URI fournie.
	 * NOT NULL
	 *
	 * @param uri URI de la collection à charger
	 * @return DtList<D> Liste correspondant à l'URI fournie
	 * @param <D> Type de l'objet
	 */
	<D extends DtObject> DtList<D> loadList(DtListURI uri);

	//==========================================================================
	//=============================== WRITE ====================================
	//==========================================================================
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
	 * Suppression d'un objet.
	 * @param uri URI de l'objet à supprimmer
	 */
	void remove(URI<? extends DtObject> uri);

}
