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
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.persistence.datastore.BrokerConfigImpl;
import io.vertigo.dynamo.impl.persistence.datastore.logical.LogicalDataStore;
import io.vertigo.dynamo.impl.persistence.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.persistence.datastore.DataStore;
import io.vertigo.lang.Assertion;

/**
 * Gestion des données mises en cache.
 *
 * @author  pchretien
 */
public final class CacheDataStore implements DataStore {
	private final DataStore logicalDataStore;
	private final CacheDataStoreConfig cacheDataStoreConfig;
	private final LogicalDataStoreConfig logicalStoreConfig;

	/**
	 * Constructeur.
	 * @param brokerConfig Configuration
	 */
	public CacheDataStore(final BrokerConfigImpl brokerConfig) {
		Assertion.checkNotNull(brokerConfig);
		//-----
		this.logicalDataStore = new LogicalDataStore(brokerConfig.getLogicalStoreConfig(), this);
		this.cacheDataStoreConfig = brokerConfig.getCacheStoreConfig();
		this.logicalStoreConfig = brokerConfig.getLogicalStoreConfig();
	}

	//==========================================================================
	//=============================== READ =====================================
	//==========================================================================
	private DataStore getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfig.getPhysicalStore(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return getPhysicalStore(dtDefinition).count(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> D load(final URI uri) {
		// - Prise en compte du cache
		if (cacheDataStoreConfig.isCacheable(uri.<DtDefinition> getDefinition())) {
			D dto = cacheDataStoreConfig.getDataCache().<D> getDtObject(uri);
			if (dto == null) {
				//Cas ou le dto représente un objet non mis en cache
				dto = this.<D> reload(uri);
			}
			return dto;
		}
		//Si on ne récupère rien dans le cache on charge depuis le store.
		return logicalDataStore.<D> load(uri);
	}

	private synchronized <D extends DtObject> D reload(final URI uri) {
		final D dto;
		if (cacheDataStoreConfig.isReloadedByList(uri.<DtDefinition> getDefinition())) {
			//On ne charge pas les cache de façon atomique.
			final DtListURI dtcURIAll = new DtListURIForCriteria<>(uri.<DtDefinition> getDefinition(), null, null);
			reloadList(dtcURIAll); //on charge la liste complete (et on remplit les caches)
			dto = cacheDataStoreConfig.getDataCache().<D> getDtObject(uri);
		} else {
			//On charge le cache de façon atomique.
			dto = logicalDataStore.<D> load(uri);
			cacheDataStoreConfig.getDataCache().putDtObject(dto);
		}
		return dto;
	}

	/** {@inheritDoc}  */
	@Override
	public <D extends DtObject> DtList<D> loadList(final DtListURI uri) {
		// - Prise en compte du cache
		//On ne met pas en cache les URI d'une association NN
		if (cacheDataStoreConfig.isCacheable(uri.getDtDefinition()) && !isMultipleAssociation(uri)) {
			DtList<D> dtc = cacheDataStoreConfig.getDataCache().getDtList(uri);
			if (dtc == null) {
				dtc = this.<D> reloadList(uri);
			}
			return dtc;
		}
		//Si la liste n'est pas dans le cache alors on lit depuis le store.
		return logicalDataStore.<D> loadList(uri);
	}

	private static boolean isMultipleAssociation(final DtListURI uri) {
		if (uri instanceof DtListURIForAssociation) {
			final DtListURIForAssociation uriForAssociation = (DtListURIForAssociation) uri;
			return !uriForAssociation.getAssociationDefinition().isAssociationSimpleDefinition();
		}
		return false;
	}

	private synchronized <D extends DtObject> DtList<D> reloadList(final DtListURI uri) {
		// On charge la liste initiale avec les critéres définis en amont
		final DtList<D> dtc = logicalDataStore.loadList(uri);
		// Mise en cache de la liste et des éléments.
		cacheDataStoreConfig.getDataCache().putDtList(dtc);
		return dtc;
	}

	//==========================================================================
	//=============================== WRITE ====================================
	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).merge(dto);
		//-----
		clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void create(final DtObject dto) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).create(dto);
		//-----
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final DtObject dto) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).update(dto);
		//-----
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final URI uri) {
		final DtDefinition dtDefinition = uri.getDefinition();
		getPhysicalStore(dtDefinition).delete(uri);
		//-----
		clearCache(dtDefinition);
	}

	/* On notifie la mise à jour du cache, celui-ci est donc vidé. */
	private void clearCache(final DtDefinition dtDefinition) {
		// On ne vérifie pas que la definition est cachable, Lucene utilise le même cache
		// A changer si on gère lucene différemment
		//	if (cacheDataStoreConfiguration.isCacheable(dtDefinition)) {
		cacheDataStoreConfig.getDataCache().clear(dtDefinition);
		//	}
	}

}
