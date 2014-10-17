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
package io.vertigo.dynamo.impl.persistence.cache;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

/**
 * Gestion des données mises en cache.
 * Centralise la dépendance à CacheManager.
 *
 * @author  pchretien
 */
final class DataCache {
	private final CacheManager cacheManager;

	/**
	 * Constructeur.
	 */
	DataCache(final CacheManager cacheManager) {
		Assertion.checkNotNull(cacheManager);
		//---------------------------------------------------------------------
		this.cacheManager = cacheManager;
	}

	/**
	 * Enregistrement d'un cache propre à un type d'objet.
	 * @param dtDefinition Définition de DT
	 * @param timeToLiveSeconds Durée de vie des éléments mis en cache
	 */
	void registerContext(final DtDefinition dtDefinition, final long timeToLiveSeconds) {
		final String context = getContext(dtDefinition);
		final int maxElementsInMemory = 1000;
		final long timeToIdleSeconds = timeToLiveSeconds / 2; //longévité déun élément non utilisé
		cacheManager.addCache("dataCache", context, maxElementsInMemory, timeToLiveSeconds, timeToIdleSeconds);
	}

	private static String getContext(final DtDefinition dtDefinition) {
		return "DataCache:" + dtDefinition.getName();
	}

	/**
	 * Récupération d'un objet potentiellement mis en cache
	 * @param uri URI du DTO
	 * @return null ou DTO 
	 */
	<D extends DtObject> D getDtObject(final URI<D> uri) {
		final DtDefinition dtDefinition = uri.getDefinition();
		return (D) cacheManager.get(getContext(dtDefinition), uri);
	}

	/**
	 * Mise à jour du cache pour un type d'objet.
	 * @param dto DTO
	 */
	void putDtObject(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		final String context = getContext(DtObjectUtil.findDtDefinition(dto));
		//2.On met à jour l'objet 
		cacheManager.put(context, createURI(dto), dto);
	}

	/**
	 * Récupération de la liste ratine objet potentiellement mise en cache
	 * @param dtcUri URI de la DTC
	 * @return null ou DTC 
	 */
	<D extends DtObject> DtList<D> getDtList(final DtListURI dtcUri) {
		Assertion.checkNotNull(dtcUri);
		//---------------------------------------------------------------------
		return (DtList<D>) cacheManager.get(getContext(dtcUri.getDtDefinition()), dtcUri);
	}

	/**
	 * Mise à jour du cache pour un type d'objet.
	 * @param dtc DTC
	 */
	void putDtList(final DtList<?> dtc) {
		Assertion.checkNotNull(dtc);
		//---------------------------------------------------------------------
		final String context = getContext(dtc.getDefinition());

		//1.On met à jour les objets 
		for (final DtObject dto : dtc) {
			cacheManager.put(context, createURI(dto), dto);
		}
		//2.Puis on met à jour la liste racine : pour que la liste ne soit pas evincée par les objets
		cacheManager.put(context, dtc.getURI(), dtc);
	}

	private static <D extends DtObject> URI<D> createURI(final D dto) {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		return new URI<>(DtObjectUtil.findDtDefinition(dto), DtObjectUtil.getId(dto));
	}

	void clear(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---------------------------------------------------------------------
		cacheManager.clear(getContext(dtDefinition));
	}
}
