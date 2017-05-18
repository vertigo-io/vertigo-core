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
package io.vertigo.dynamo.impl.store.datastore.cache;

import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.VCollectors;
import io.vertigo.dynamo.impl.store.StoreEvent;
import io.vertigo.dynamo.impl.store.datastore.DataStoreConfigImpl;
import io.vertigo.dynamo.impl.store.datastore.DataStorePlugin;
import io.vertigo.dynamo.impl.store.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;

/**
 * Gestion des données mises en cache.
 *
 * @author  pchretien
 */
public final class CacheDataStore {
	private final CollectionsManager collectionsManager;
	private final StoreManager storeManager;
	private final CacheDataStoreConfig cacheDataStoreConfig;
	private final LogicalDataStoreConfig logicalStoreConfig;

	/**
	 * Constructor.
	 * @param storeManager Store manager
	 * @param eventBusManager Event bus manager
	 * @param dataStoreConfig Data store configuration
	 */
	public CacheDataStore(final CollectionsManager collectionsManager, final StoreManager storeManager, final EventBusManager eventBusManager, final DataStoreConfigImpl dataStoreConfig) {
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(eventBusManager);
		Assertion.checkNotNull(dataStoreConfig);
		//-----
		this.collectionsManager = collectionsManager;
		this.storeManager = storeManager;
		cacheDataStoreConfig = dataStoreConfig.getCacheStoreConfig();
		logicalStoreConfig = dataStoreConfig.getLogicalStoreConfig();
		eventBusManager.subscribe(
				StoreEvent.class,
				event -> clearCache(event.getUri().getDefinition()));
	}

	private DataStorePlugin getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfig.getPhysicalDataStore(dtDefinition);
	}

	/**
	 * @param <E> the type of entity
	 * @param uri Element uri
	 * @return Element by uri
	 */
	public <E extends Entity> E readNullable(final URI<E> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		E entity;
		if (cacheDataStoreConfig.isCacheable(dtDefinition)) {
			// - Prise en compte du cache
			entity = cacheDataStoreConfig.getDataCache().getDtObject(uri);
			// - Prise en compte du cache
			if (entity == null) {
				//Cas ou le dto représente un objet non mis en cache
				entity = this.<E> loadNullable(dtDefinition, uri);
			}
		} else {
			entity = getPhysicalStore(dtDefinition).readNullable(dtDefinition, uri);
		}
		return entity;
	}

	private synchronized <E extends Entity> E loadNullable(final DtDefinition dtDefinition, final URI<E> uri) {
		final E entity;
		if (cacheDataStoreConfig.isReloadedByList(dtDefinition)) {
			//On ne charge pas les cache de façon atomique.
			final DtListURI dtcURIAll = new DtListURIForCriteria<>(dtDefinition, null, null);
			loadList(dtcURIAll); //on charge la liste complete (et on remplit les caches)
			entity = cacheDataStoreConfig.getDataCache().getDtObject(uri);
		} else {
			//On charge le cache de façon atomique à partir du dataStore
			entity = getPhysicalStore(dtDefinition).readNullable(dtDefinition, uri);
			if (entity != null) {
				cacheDataStoreConfig.getDataCache().putDtObject(entity);
			}
		}
		return entity;
	}

	private <E extends Entity> DtList<E> doLoadList(final DtDefinition dtDefinition, final DtListURI listUri) {
		Assertion.checkNotNull(listUri);
		//-----
		final DtList<E> list;
		if (listUri instanceof DtListURIForMasterData) {
			list = loadMDList((DtListURIForMasterData) listUri);
		} else if (listUri instanceof DtListURIForSimpleAssociation) {
			list = getPhysicalStore(dtDefinition).findAll(dtDefinition, (DtListURIForSimpleAssociation) listUri);
		} else if (listUri instanceof DtListURIForNNAssociation) {
			list = getPhysicalStore(dtDefinition).findAll(dtDefinition, (DtListURIForNNAssociation) listUri);
		} else if (listUri instanceof DtListURIForCriteria<?>) {
			final DtListURIForCriteria<E> castedListUri = DtListURIForCriteria.class.cast(listUri);
			list = getPhysicalStore(dtDefinition).findAll(dtDefinition, castedListUri);
		} else {
			throw new IllegalArgumentException("cas non traité " + listUri);
		}
		return new DtList(list, listUri);
	}

	private <E extends Entity> DtList<E> loadMDList(final DtListURIForMasterData uri) {
		Assertion.checkNotNull(uri);
		Assertion.checkArgument(uri.getDtDefinition().getSortField().isPresent(), "Sortfield on definition {0} wasn't set. It's mandatory for MasterDataList.", uri.getDtDefinition().getName());
		//-----
		//On cherche la liste complete
		final DtList<E> unFilteredDtc = findAll(new DtListURIForCriteria<E>(uri.getDtDefinition(), null, null));

		//On compose les fonctions
		//1.on filtre
		//2.on trie
		final DtList list = unFilteredDtc
				.stream()
				.filter(storeManager.getMasterDataConfig().getFilter(uri))
				.collect(VCollectors.toDtList(unFilteredDtc.getDefinition()));
		return collectionsManager.sort(list, uri.getDtDefinition().getSortField().get().getName(), false);
	}

	/**
	 * @param <E> the type of entity
	 * @param uri List uri
	 * @return List of this uri
	 */
	public <E extends Entity> DtList<E> findAll(final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//-----
		//- Prise en compte du cache
		//On ne met pas en cache les URI d'une association NN
		if (cacheDataStoreConfig.isCacheable(uri.getDtDefinition()) && !isMultipleAssociation(uri)) {
			DtList<E> list = cacheDataStoreConfig.getDataCache().getDtList(uri);
			if (list == null) {
				list = this.<E> loadList(uri);
			}
			return list;
		}
		//Si la liste n'est pas dans le cache alors on lit depuis le store.
		return doLoadList(uri.getDtDefinition(), uri);
	}

	private static boolean isMultipleAssociation(final DtListURI uri) {
		return uri instanceof DtListURIForNNAssociation;
	}

	private synchronized <E extends Entity> DtList<E> loadList(final DtListURI uri) {
		// On charge la liste initiale avec les critéres définis en amont
		final DtList<E> list = doLoadList(uri.getDtDefinition(), uri);
		// Mise en cache de la liste et des éléments.
		cacheDataStoreConfig.getDataCache().putDtList(list);
		return list;
	}

	/* On notifie la mise à jour du cache, celui-ci est donc vidé. */
	private void clearCache(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		// On ne vérifie pas que la definition est cachable, Lucene utilise le même cache
		// A changer si on gère lucene différemment
		cacheDataStoreConfig.getDataCache().clear(dtDefinition);
	}
}
