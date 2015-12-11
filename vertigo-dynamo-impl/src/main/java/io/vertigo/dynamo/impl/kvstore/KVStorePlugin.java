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
package io.vertigo.dynamo.impl.kvstore;

import io.vertigo.lang.Option;
import io.vertigo.lang.Plugin;

import java.util.List;

/**
 * This plugin defines the strategy used to store a 'collection' of objects, identified by their name.
 *
 * @author pchretien
 */
public interface KVStorePlugin extends Plugin {

	/**
	 * @return list of collections names 
	 */
	List<String> getCollections();

	/**
	 * Add a new element in the store.
	 * @param collection Name of the collection
	 * @param id Id of the elemnt
	 * @param objet element to add
	 */
	void put(String collection, String id, Object objet);

	/**
	 * Removes an element defined by an id.
	 * @param collection Name of the collection
	 * @param id Id of the element to remove
	 */
	void remove(String collection, String id);

	/**
	 * Find the element corresponding to an id in a specified collection.
	 * If element not found an empty option is returned.
	 * @param collection Name of the collection
	 * @param clazz Type of the element
	 * @return the element corresponding to the id as an option.
	 */
	<C> Option<C> find(String collection, String id, Class<C> clazz);

	/**
	 * Find all the elements of a collection in a range defined by (skip, limit).  
	 * @param collection Name of the collection
	 * @param skip Skips the first elements 
	 * @param limit Limit size of the number of elements 	
	 * @param clazz Type of elements
	 * @return the list of elements
	 */
	<C> List<C> findAll(String collection, int skip, Integer limit, Class<C> clazz);
}
