/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import io.vertigo.core.component.Plugin;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;

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
	 *
	 * @param uri URI de l'objet à charger
	 * @param <E> the type of entity
	 * @param dtDefinition Definition
	 * @return D correspondant à l'URI fournie.
	 */
	<E extends Entity> E readNullable(DtDefinition dtDefinition, URI<E> uri);

	/**
	 * Récupération d'une liste correspondant à l'URI fournie.
	 * NOT NULL
	 *
	 * @param uri URI de la collection à charger
	 * @param dtDefinition Definition
	 * @return DtList<D> Liste correspondant à l'URI fournie
	 * @param <E> the type of entity
	 */
	<E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForNNAssociation uri);

	/**
	 * Récupération d'une liste correspondant à l'URI fournie.
	 * NOT NULL
	 *
	 * @param uri URI de la collection à charger
	 * @param dtDefinition Definition
	 * @return DtList<D> Liste correspondant à l'URI fournie
	 * @param <E> the type of entity
	 */
	<E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForSimpleAssociation uri);

	/**
	 * Récupération d'une liste correspondant à l'URI fournie.
	 * NOT NULL
	 *
	 * @param uri URI de la collection à charger
	 * @param dtDefinition Definition
	 * @return DtList<D> Liste correspondant à l'URI fournie
	 * @param <E> the type of entity
	 */
	<E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForCriteria<E> uri);

	//==========================================================================
	//=============================== WRITE ====================================
	//==========================================================================
	/**
	 * Creates an object.
	 * No object with the same id must have been created previously.
	 *
	 * @param dtDefinition Definition
	 * @param entity Object to create
	 * @return the created entity
	 */
	<E extends Entity> E create(DtDefinition dtDefinition, E entity);

	/**
	 * Updates an object.
	 * This object must have an id.
	 * @param dtDefinition Definition
	 * @param entity Object to update
	 */
	void update(DtDefinition dtDefinition, Entity entity);

	/**
	 * Deletes an object identified by an uri.
	 * @param dtDefinition Definition
	 * @param uri URI
	 */
	void delete(DtDefinition dtDefinition, URI<?> uri);

	/**
	 * Loads for update.
	 *
	 * @param dtDefinition Object's definition
	 * @param uri Object's uri
	 * @param <E> the type of entity
	 * @return D Object value.
	 */
	<E extends Entity> E readNullableForUpdate(DtDefinition dtDefinition, URI<?> uri);

	/**
	 * Finds a lists of entities matching a criteria.
	 * @param dtDefinition the definition of entities to find
	 * @param criteria the criteria to match
	 * @param maxRows max number of rows to retrieve
	 * @return the list
	 */
	<E extends Entity> DtList<E> findByCriteria(final DtDefinition dtDefinition, final Criteria<E> criteria, final Integer maxRows);

}
