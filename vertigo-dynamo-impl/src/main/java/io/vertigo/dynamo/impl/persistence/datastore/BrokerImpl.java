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

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.persistence.datastore.cache.CacheDataStore;
import io.vertigo.dynamo.impl.persistence.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.persistence.datastore.Broker;
import io.vertigo.dynamo.persistence.datastore.DataStore;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionResourceId;
import io.vertigo.lang.Assertion;

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

	/**
	 * Constructeur.
	 * Une fois le broker construit la configuration est bloquée.
	 * @param brokerConfig Configuration du broker
	 */
	public BrokerImpl(final BrokerConfigImpl brokerConfig) {
		Assertion.checkNotNull(brokerConfig);
		//-----
		//On vérrouille la configuration.
		//brokerConfiguration.lock();
		//On crée la pile de Store.
		logicalStoreConfig = brokerConfig.getLogicalStoreConfig();
		cacheDataStore = new CacheDataStore(brokerConfig);
	}

	private DataStore getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfig.getPhysicalDataStore(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void workOn(final URI<? extends DtObject> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		getPhysicalStore(dtDefinition).lockForUpdate(dtDefinition, uri);
		//-----
		obtainEventTx().fire("update", uri.toURN());
	}

	//--- Transactionnal Event
	/**
	 * Identifiant de ressource Event.
	 */
	public static final VTransactionResourceId<EventTransactionResource> EVENT_RESOURCE_ID = new VTransactionResourceId<>(VTransactionResourceId.Priority.LOW, "Events");

	private EventTransactionResource obtainEventTx() {
		EventTransactionResource eventTransactionResource = getTransactionManager().getCurrentTransaction().getResource(EVENT_RESOURCE_ID);
		if (eventTransactionResource == null) { //by convention resource is null if not already registered
			eventTransactionResource = new EventTransactionResource();
			getTransactionManager().getCurrentTransaction().addResource(EVENT_RESOURCE_ID, eventTransactionResource);

			//register cache flush listener
			//TODO move to a upper layer
			final EventListener cacheClearEventListener = new CacheClearEventListener(cacheDataStore);
			eventTransactionResource.subscribe("update", cacheClearEventListener);
			eventTransactionResource.subscribe("create", cacheClearEventListener);
			eventTransactionResource.subscribe("delete", cacheClearEventListener);

			//register searchDirty listener
			//TODO move to a upper layer
			//final SearchManager searchManager = Home.getComponentSpace().resolve(SearchManager.class);
			final EventListener searchIndexDirtyEventListener = new SearchIndexDirtyEventListener(/*searchManager*/);
			eventTransactionResource.subscribe("update", searchIndexDirtyEventListener);
			eventTransactionResource.subscribe("create", searchIndexDirtyEventListener);
			eventTransactionResource.subscribe("delete", searchIndexDirtyEventListener);
		}
		return eventTransactionResource;
	}

	private static final class CacheClearEventListener implements EventListener {

		private final CacheDataStore cacheDataStore;

		CacheClearEventListener(final CacheDataStore cacheDataStore) {
			this.cacheDataStore = cacheDataStore;
		}

		@Override
		public void onEvent(final String event) {
			final URI uri = URI.fromURN(event);
			cacheDataStore.clearCache(uri.getDefinition());
		}
	}

	private static final class SearchIndexDirtyEventListener implements EventListener {

		//private final SearchManager searchManager;

		//SearchIndexDirtyEventListener(final SearchManager searchManager) {
		//	this.searchManager = searchManager;
		//}

		@Override
		public void onEvent(final String event) {
			//final URI uri = URI.fromURN(event);
			//TODO searchManager.markSubjectDirty(uri);
		}
	}

	private static VTransactionManager getTransactionManager() {
		return Home.getComponentSpace().resolve(VTransactionManager.class);
	}

	//--- Transactionnal Event

	/** {@inheritDoc} */
	@Override
	public void create(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).create(dtDefinition, dto);
		//-----
		obtainEventTx().fire("create", new URI(dtDefinition, DtObjectUtil.getId(dto)).toURN());
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		//cacheDataStore.clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).update(dtDefinition, dto);
		//-----
		obtainEventTx().fire("update", new URI(dtDefinition, DtObjectUtil.getId(dto)).toURN());
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		//cacheDataStore.clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).merge(dtDefinition, dto);
		//-----
		obtainEventTx().fire("update", new URI(dtDefinition, DtObjectUtil.getId(dto)).toURN());
		//cacheDataStore.clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final URI<? extends DtObject> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		getPhysicalStore(dtDefinition).delete(dtDefinition, uri);
		//-----
		obtainEventTx().fire("delete", uri.toURN());
		//cacheDataStore.clearCache(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> D get(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//----------------------------------------------------------------------
		final D dto = cacheDataStore.<D> load(uri);
		//----------------------------------------------------------------------
		Assertion.checkNotNull(dto, "L''objet {0} n''a pas été trouvé", uri);
		return dto;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> DtList<D> getList(final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtList<D> dtc = cacheDataStore.loadList(uri);
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
