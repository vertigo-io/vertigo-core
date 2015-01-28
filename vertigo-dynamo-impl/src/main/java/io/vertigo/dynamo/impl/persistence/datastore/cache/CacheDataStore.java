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
import io.vertigo.dynamo.persistence.datastore.DataStore;
import io.vertigo.lang.Assertion;

/**
 * Gestion des données mises en cache.
 *
 * @author  pchretien
 */
public final class CacheDataStore implements DataStore {
	private final DataStore logicalDataStore;
	private final CacheDataStoreConfig cacheDataStoreConfiguration;

	/**
	 * Constructeur.
	 * @param logicalStore Store logique
	 * @param cacheDataStoreConfiguration Configuration du cache
	 */
	public CacheDataStore(final DataStore logicalStore, final CacheDataStoreConfig cacheDataStoreConfiguration) {
		Assertion.checkNotNull(cacheDataStoreConfiguration);
		Assertion.checkNotNull(logicalStore);
		//-----
		this.logicalDataStore = logicalStore;
		this.cacheDataStoreConfiguration = cacheDataStoreConfiguration;
	}

	//==========================================================================
	//=============================== READ =====================================
	//==========================================================================

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return logicalDataStore.count(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> D load(final URI uri) {
		// - Prise en compte du cache
		if (cacheDataStoreConfiguration.isCacheable(uri.<DtDefinition> getDefinition())) {
			D dto = cacheDataStoreConfiguration.getDataCache().<D> getDtObject(uri);
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
		if (cacheDataStoreConfiguration.isReloadedByList(uri.<DtDefinition> getDefinition())) {
			//On ne charge pas les cache de façon atomique.
			final DtListURI dtcURIAll = new DtListURIForCriteria<>(uri.<DtDefinition> getDefinition(), null, null);
			reloadList(dtcURIAll); //on charge la liste complete (et on remplit les caches)
			dto = cacheDataStoreConfiguration.getDataCache().<D> getDtObject(uri);
		} else {
			//On charge le cache de façon atomique.
			dto = logicalDataStore.<D> load(uri);
			cacheDataStoreConfiguration.getDataCache().putDtObject(dto);
		}
		return dto;
	}

	/** {@inheritDoc}  */
	@Override
	public <D extends DtObject> DtList<D> loadList(final DtListURI uri) {
		// - Prise en compte du cache
		//On ne met pas en cache les URI d'une association NN
		if (cacheDataStoreConfiguration.isCacheable(uri.getDtDefinition()) && !isMultipleAssociation(uri)) {
			DtList<D> dtc = cacheDataStoreConfiguration.getDataCache().getDtList(uri);
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
		cacheDataStoreConfiguration.getDataCache().putDtList(dtc);
		return dtc;
	}

	//==========================================================================
	//=============================== WRITE ====================================
	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		logicalDataStore.merge(dto);
		//-----
		clearCache(DtObjectUtil.findDtDefinition(dto));
	}

	/** {@inheritDoc} */
	@Override
	public void create(final DtObject dto) {
		logicalDataStore.create(dto);
		//-----
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		clearCache(DtObjectUtil.findDtDefinition(dto));
	}

	/** {@inheritDoc} */
	@Override
	public void update(final DtObject dto) {
		logicalDataStore.update(dto);
		//-----
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		clearCache(DtObjectUtil.findDtDefinition(dto));
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final URI uri) {
		logicalDataStore.delete(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		clearCache(dtDefinition);
	}

	/* On notifie la mise à jour du cache, celui-ci est donc vidé. */
	private void clearCache(final DtDefinition dtDefinition) {
		// On ne vérifie pas que la definition est cachable, Lucene utilise le même cache
		// A changer si on gère lucene différemment
		//	if (cacheDataStoreConfiguration.isCacheable(dtDefinition)) {
		cacheDataStoreConfiguration.getDataCache().clear(dtDefinition);
		//	}
	}

}
