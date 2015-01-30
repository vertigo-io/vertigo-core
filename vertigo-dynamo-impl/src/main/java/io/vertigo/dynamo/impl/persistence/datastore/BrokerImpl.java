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
package io.vertigo.dynamo.impl.persistence.datastore;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.persistence.datastore.cache.CacheDataStore;
import io.vertigo.dynamo.impl.persistence.datastore.cache.CacheDataStoreConfig;
import io.vertigo.dynamo.impl.persistence.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.persistence.datastore.Broker;
import io.vertigo.dynamo.persistence.datastore.DataStore;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

/**
 * Implémentation Standard du Broker.
 * Cette implémentation s'appuie sur le concept de Store.
 * Un store définit les modalités du stockage
 * alors que le broker se concentre sur la problématique des accès aux ressources.
 * @author pchretien
 */
public final class BrokerImpl implements Broker {
	/** Le store est le point d'accès unique à la base (sql, xml, fichier plat...). */
	private final CacheDataStore cacheDataStore;
	private final LogicalDataStoreConfig logicalStoreConfig;
	private final CacheDataStoreConfig cacheDataStoreConfig;

	/**
	 * Constructeur.
	 * Une fois le broker construit la configuration est bloquée.
	 * @param brokerConfiguration Configuration du broker
	 */
	public BrokerImpl(final BrokerConfigImpl brokerConfiguration) {
		Assertion.checkNotNull(brokerConfiguration);
		//-----
		//On vérouille la configuration.
		//brokerConfiguration.lock();
		//On crée la pile de Store.
		this.logicalStoreConfig = brokerConfiguration.getLogicalStoreConfig();
		this.cacheDataStoreConfig = brokerConfiguration.getCacheStoreConfig();
		cacheDataStore = new CacheDataStore(brokerConfiguration);
	}

	private DataStore getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfig.getPhysicalStore(dtDefinition);
	}

	/* On notifie la mise à jour du cache, celui-ci est donc vidé. */
	private void clearCache(final DtDefinition dtDefinition) {
		// On ne vérifie pas que la definition est cachable, Lucene utilise le même cache
		// A changer si on gère lucene différemment
		//	if (cacheDataStoreConfiguration.isCacheable(dtDefinition)) {
		cacheDataStoreConfig.getDataCache().clear(dtDefinition);
		//	}
	}

	//==========================================================================
	//===================Méthodes publiques du Broker===========================
	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public void create(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).create(dtDefinition, dto);
		//-----
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void save(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		if (DtObjectUtil.getId(dto) == null) {
			create(dto);
		} else {
			update(dto);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void update(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).update(dtDefinition, dto);
		//-----
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).merge(dtDefinition, dto);
		//-----
		clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final URI uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		getPhysicalStore(dtDefinition).delete(dtDefinition, uri);
		//-----
		clearCache(dtDefinition);
	}

	@Override
	@Deprecated
	public <D extends DtObject> D get(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final D dto = cacheDataStore.<D> load(uri.getDefinition(), uri);
		//-----
		return Option.option(dto).get();
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> Option<D> getOption(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final D dto = cacheDataStore.<D> load(uri.getDefinition(), uri);
		//-----
		return Option.option(dto);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> DtList<D> getList(final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtList<D> dtc = cacheDataStore.loadList(uri.getDtDefinition(), uri);
		//-----
		Assertion.checkNotNull(dtc);
		return dtc;
	}

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return getPhysicalStore(dtDefinition).count(dtDefinition);
	}
}
