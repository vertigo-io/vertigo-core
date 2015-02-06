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
package io.vertigo.dynamo.impl.persistence.datastore.cache;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.impl.persistence.datastore.BrokerConfigImpl;
import io.vertigo.dynamo.impl.persistence.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.persistence.datastore.DataStore;
import io.vertigo.lang.Assertion;

/**
 * Gestion des données mises en cache.
 *
 * @author  pchretien
 */
public final class CacheDataStore {
	private final CacheDataStoreConfig cacheDataStoreConfig;
	private final LogicalDataStoreConfig logicalStoreConfig;

	/**
	 * Constructeur.
	 * @param brokerConfig Configuration
	 */
	public CacheDataStore(final BrokerConfigImpl brokerConfig) {
		Assertion.checkNotNull(brokerConfig);
		//-----
		this.cacheDataStoreConfig = brokerConfig.getCacheStoreConfig();
		this.logicalStoreConfig = brokerConfig.getLogicalStoreConfig();
	}

	private DataStore getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfig.getPhysicalStore(dtDefinition);
	}

	public <D extends DtObject> D load(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		D dto;
		if (cacheDataStoreConfig.isCacheable(dtDefinition)) {
			// - Prise en compte du cache
			dto = cacheDataStoreConfig.getDataCache().getDtObject(uri);
			// - Prise en compte du cache
			if (dto == null) {
				//Cas ou le dto représente un objet non mis en cache
				dto = this.<D> reload(dtDefinition, uri);
			}
		} else {
			dto = getPhysicalStore(dtDefinition).load(dtDefinition, uri);
		}
		return dto;
	}

	private synchronized <D extends DtObject> D reload(final DtDefinition dtDefinition, final URI<D> uri) {
		final D dto;
		if (cacheDataStoreConfig.isReloadedByList(dtDefinition)) {
			//On ne charge pas les cache de façon atomique.
			final DtListURI dtcURIAll = new DtListURIForCriteria<>(dtDefinition, null, null);
			reloadList(dtcURIAll); //on charge la liste complete (et on remplit les caches)
			dto = cacheDataStoreConfig.getDataCache().getDtObject(uri);
		} else {
			//On charge le cache de façon atomique à partir du dataStore
			dto = getPhysicalStore(dtDefinition).load(dtDefinition, uri);
			cacheDataStoreConfig.getDataCache().putDtObject(dto);
		}
		return dto;
	}

	private <D extends DtObject> DtList<D> doLoadList(final DtDefinition dtDefinition, final DtListURI listUri) {
		Assertion.checkNotNull(listUri);
		//-----
		final DtList<D> dtc;
		if (listUri instanceof DtListURIForMasterData) {
			dtc = loadMDList((DtListURIForMasterData) listUri);
		} else if (listUri instanceof DtListURIForSimpleAssociation) {
			dtc = getPhysicalStore(dtDefinition).loadList(dtDefinition, (DtListURIForSimpleAssociation) listUri);
		} else if (listUri instanceof DtListURIForNNAssociation) {
			dtc = getPhysicalStore(dtDefinition).loadList(dtDefinition, (DtListURIForNNAssociation) listUri);
		} else if (listUri instanceof DtListURIForCriteria<?>) {
			dtc = getPhysicalStore(dtDefinition).loadList(dtDefinition, (DtListURIForCriteria<D>) listUri);
		} else {
			throw new IllegalArgumentException("cas non traité " + listUri);
		}
		dtc.setURI(listUri);
		return dtc;
	}

	private <D extends DtObject> DtList<D> loadMDList(final DtListURIForMasterData uri) {
		Assertion.checkNotNull(uri);
		Assertion.checkArgument(uri.getDtDefinition().getSortField().isDefined(), "Sortfield on definition {0} wasn't set. It's mandatory for MasterDataList.", uri.getDtDefinition().getName());
		//-----
		//On cherche la liste complete
		final DtList<D> unFilteredDtc = loadList(new DtListURIForCriteria<D>(uri.getDtDefinition(), null, null));

		//On compose les fonctions
		//1.on filtre
		//2.on trie
		return logicalStoreConfig.getPersistenceManager().getMasterDataConfig().getFilter(uri)
				.sort(uri.getDtDefinition().getSortField().get().getName(), false, true, true)
				.apply(unFilteredDtc);
	}

	public <D extends DtObject> DtList<D> loadList(final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//-----
		//- Prise en compte du cache
		//On ne met pas en cache les URI d'une association NN
		if (cacheDataStoreConfig.isCacheable(uri.getDtDefinition()) && !isMultipleAssociation(uri)) {
			DtList<D> dtc = cacheDataStoreConfig.getDataCache().getDtList(uri);
			if (dtc == null) {
				dtc = this.<D> reloadList(uri);
			}
			return dtc;
		}
		//Si la liste n'est pas dans le cache alors on lit depuis le store.
		return doLoadList(uri.getDtDefinition(), uri);
	}

	private static boolean isMultipleAssociation(final DtListURI uri) {
		return uri instanceof DtListURIForNNAssociation;
	}

	private synchronized <D extends DtObject> DtList<D> reloadList(final DtListURI uri) {
		// On charge la liste initiale avec les critéres définis en amont
		final DtList<D> dtc = doLoadList(uri.getDtDefinition(), uri);
		// Mise en cache de la liste et des éléments.
		cacheDataStoreConfig.getDataCache().putDtList(dtc);
		return dtc;
	}

	/* On notifie la mise à jour du cache, celui-ci est donc vidé. */
	public void clearCache(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		// On ne vérifie pas que la definition est cachable, Lucene utilise le même cache
		// A changer si on gère lucene différemment
		cacheDataStoreConfig.getDataCache().clear(dtDefinition);
	}
}
