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
package io.vertigo.dynamo.impl.store.datastore;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.lang.Plugin;

/**
 * The DataStorePlugin class defines the logical way used to read and write data in a data store.
 * This plugin can be implemented in a sql or no sql way.
 *
 * this datatore is linked to a dataSpace.
 * Several dtDefinition can be included in a dataSpace.
 *
 * @author  pchretien
 */
public interface DataStorePlugin extends Plugin {

	/**
	 * @return the dataSpace
	 */
	String getDataSpace();

	/**
	 * @return the name of the connection
	 */
	String getConnectionName();

	//==========================================================================
	//=============================== READ =====================================
	//==========================================================================

	/**
	 * Returns the number of elements contained in the definition.
	 * @param dtDefinition Définition de DT
	 * @return the number of elements
	 */
	int count(final DtDefinition dtDefinition);

	/**
	 * Récupération de l'objet correspondant à l'URI fournie.
	 * Peut-être null.
	 *
	 * @param uri URI de l'objet à charger
	 * @param <D> Type de l'objet
	 * @param dtDefinition Definition
	 * @return D correspondant à l'URI fournie.
	 */
	<D extends DtObject> D load(DtDefinition dtDefinition, URI<D> uri);

	/**
	 * Récupération d'une liste correspondant à l'URI fournie.
	 * NOT NULL
	 *
	 * @param uri URI de la collection à charger
	 * @param dtDefinition Definition
	 * @return DtList<D> Liste correspondant à l'URI fournie
	 * @param <D> Type de l'objet
	 */
	<D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final DtListURIForNNAssociation uri);

	/**
	 * Récupération d'une liste correspondant à l'URI fournie.
	 * NOT NULL
	 *
	 * @param uri URI de la collection à charger
	 * @param dtDefinition Definition
	 * @return DtList<D> Liste correspondant à l'URI fournie
	 * @param <D> Type de l'objet
	 */
	<D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final DtListURIForSimpleAssociation uri);

	/**
	 * Récupération d'une liste correspondant à l'URI fournie.
	 * NOT NULL
	 *
	 * @param uri URI de la collection à charger
	 * @param dtDefinition Definition
	 * @return DtList<D> Liste correspondant à l'URI fournie
	 * @param <D> Type de l'objet
	 */
	<D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final DtListURIForCriteria<D> uri);

	//==========================================================================
	//=============================== WRITE ====================================
	//==========================================================================
	/**
	* Creates an object.
	* No object with the same id must have been created previously.
	*
	* @param dtDefinition Definition
	* @param dto Object to create
	*/
	void create(DtDefinition dtDefinition, DtObject dto);

	/**
	* Updates an object.
	* This object must have an id.
	* @param dtDefinition Definition
	* @param dto Object to update
	*/
	void update(DtDefinition dtDefinition, DtObject dto);

	/**
	* Merges an object.
	* Strategy to create or update this object depends on the state of the database.
	*
	*  - If  this object is already created : update
	*  - If  this object is not found : create
	*
	* @param dtDefinition Definition
	* @param dto Object to merge
	*/
	void merge(DtDefinition dtDefinition, DtObject dto);

	/**
	 * Deletes an object identified by an uri.
	 * @param dtDefinition Definition
	 * @param uri URI
	 */
	void delete(DtDefinition dtDefinition, URI uri);

	/**
	 * Locks for update.
	 * @param dtDefinition Object's definition
	 * @param uri Object's uri
	 */
	void lockForUpdate(DtDefinition dtDefinition, URI uri);
}
