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

import io.vertigo.app.Home;
import io.vertigo.commons.event.EventManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.store.datastore.cache.CacheDataStore;
import io.vertigo.dynamo.impl.store.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.store.datastore.DataStorePlugin;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionResourceId;
import io.vertigo.lang.Assertion;

/**
 * Implementation of DataStore.
 * @author pchretien
 */
public final class DataStoreImpl implements DataStore {
	/** Le store est le point d'accès unique à la base (sql, xml, fichier plat...). */
	private final CacheDataStore cacheDataStore;
	private final LogicalDataStoreConfig logicalStoreConfig;
	private final EventManager eventsManager;

	/**
	 * Constructeur.
	 * Une fois le dataStore construit la configuration est bloquée.
	 * @param dataStoreConfig config of the dataStore
	 */
	public DataStoreImpl(final DataStoreConfigImpl dataStoreConfig) {
		Assertion.checkNotNull(dataStoreConfig);
		//-----
		logicalStoreConfig = dataStoreConfig.getLogicalStoreConfig();
		cacheDataStore = new CacheDataStore(dataStoreConfig);
		eventsManager = dataStoreConfig.getEventsManager();
	}

	private DataStorePlugin getPhysicalStore(final DtDefinition dtDefinition) {
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
		obtainEventTx().fireOnCommit(StoreManager.FiredEvent.storeUpdate, uri);
	}

	//--- Transactionnal Event
	/**
	 * Identifiant de ressource Event.
	 */
	public static final VTransactionResourceId<EventTransactionResource<URI>> EVENT_RESOURCE_ID = new VTransactionResourceId<>(VTransactionResourceId.Priority.LOW, "Events");

	private EventTransactionResource<URI> obtainEventTx() {
		EventTransactionResource<URI> eventTransactionResource = getTransactionManager().getCurrentTransaction().getResource(EVENT_RESOURCE_ID);
		if (eventTransactionResource == null) { //by convention resource is null if not already registered
			eventTransactionResource = new EventTransactionResource<>(eventsManager);
			getTransactionManager().getCurrentTransaction().addResource(EVENT_RESOURCE_ID, eventTransactionResource);
		}
		return eventTransactionResource;
	}

	private static VTransactionManager getTransactionManager() {
		return Home.getApp().getComponentSpace().resolve(VTransactionManager.class);
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
		obtainEventTx().fireOnCommit(StoreManager.FiredEvent.storeCreate, new URI(dtDefinition, DtObjectUtil.getId(dto)));
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
	}

	/** {@inheritDoc} */
	@Override
	public void update(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).update(dtDefinition, dto);
		//-----
		obtainEventTx().fireOnCommit(StoreManager.FiredEvent.storeUpdate, new URI(dtDefinition, DtObjectUtil.getId(dto)));
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
	}

	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		getPhysicalStore(dtDefinition).merge(dtDefinition, dto);
		//-----
		obtainEventTx().fireOnCommit(StoreManager.FiredEvent.storeUpdate, new URI(dtDefinition, DtObjectUtil.getId(dto)));
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final URI<? extends DtObject> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		getPhysicalStore(dtDefinition).delete(dtDefinition, uri);
		//-----
		obtainEventTx().fireOnCommit(StoreManager.FiredEvent.storeDelete, uri);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> D get(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final D dto = cacheDataStore.<D> load(uri);
		//-----
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
