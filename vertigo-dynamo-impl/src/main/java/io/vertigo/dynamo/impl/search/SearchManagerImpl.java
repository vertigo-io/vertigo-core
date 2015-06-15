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
package io.vertigo.dynamo.impl.search;

import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.events.EventsManager;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Implémentation standard du gestionnaire des indexes de recherche.
 * @author dchallas
 */
public final class SearchManagerImpl implements SearchManager, Activeable {

	private final VTransactionManager transactionManager;
	private final SearchServicesPlugin searchServicesPlugin;

	private final ScheduledExecutorService executorService; //TODO : replace by WorkManager to make distributed work easier
	private final Map<String, List<URI<? extends KeyConcept>>> dirtyElementsPerIndexName = new HashMap<>();

	/**
	 * Constructor.
	 * @param searchServicesPlugin Search plugin
	 * @param eventsManager Events Manager
	 * @param transactionManager Transaction Manager
	 */
	@Inject
	public SearchManagerImpl(final SearchServicesPlugin searchServicesPlugin, final EventsManager eventsManager, final VTransactionManager transactionManager) {
		Assertion.checkNotNull(searchServicesPlugin);
		Assertion.checkNotNull(eventsManager);
		Assertion.checkNotNull(transactionManager);
		//-----
		this.searchServicesPlugin = searchServicesPlugin;

		final SearchIndexDirtyEventListener searchIndexDirtyEventListener = new SearchIndexDirtyEventListener(this);
		eventsManager.register(PersistenceManager.FiredEvent.storeCreate, false, searchIndexDirtyEventListener);
		eventsManager.register(PersistenceManager.FiredEvent.storeUpdate, false, searchIndexDirtyEventListener);
		eventsManager.register(PersistenceManager.FiredEvent.storeDelete, false, searchIndexDirtyEventListener);

		executorService = Executors.newSingleThreadScheduledExecutor();
		this.transactionManager = transactionManager;
	}

	@Override
	public void start() {
		for (final SearchIndexDefinition indexDefinition : Home.getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			dirtyElementsPerIndexName.put(indexDefinition.getName(), new ArrayList<URI<? extends KeyConcept>>());
		}
	}

	@Override
	public void stop() {
		//nothing
	}

	/** {@inheritDoc} */
	@Override
	public <S extends KeyConcept, I extends DtObject> void putAll(final SearchIndexDefinition indexDefinition, final Collection<SearchIndex<S, I>> indexCollection) {
		searchServicesPlugin.putAll(indexDefinition, indexCollection);
	}

	/** {@inheritDoc} */
	@Override
	public <S extends KeyConcept, I extends DtObject> void put(final SearchIndexDefinition indexDefinition, final SearchIndex<S, I> index) {
		searchServicesPlugin.put(indexDefinition, index);
	}

	/** {@inheritDoc} */
	@Override
	public <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState) {
		return searchServicesPlugin.loadList(indexDefinition, searchQuery, listState);
	}

	/** {@inheritDoc} */
	@Override
	public long count(final SearchIndexDefinition indexDefinition) {
		return searchServicesPlugin.count(indexDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public <S extends KeyConcept> void remove(final SearchIndexDefinition indexDefinition, final URI<S> uri) {
		searchServicesPlugin.remove(indexDefinition, uri);
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll(final SearchIndexDefinition indexDefinition, final ListFilter listFilter) {
		searchServicesPlugin.remove(indexDefinition, listFilter);
	}

	/** {@inheritDoc} */
	@Override
	public SearchIndexDefinition findIndexDefinitionByKeyConcept(final Class<? extends KeyConcept> keyConceptClass) {
		final SearchIndexDefinition indexDefinition = findIndexDefinitionByKeyConcept(DtObjectUtil.findDtDefinition(keyConceptClass));
		Assertion.checkNotNull(indexDefinition, "No SearchIndexDefinition was defined for this keyConcept : {0}", keyConceptClass.getSimpleName());
		return indexDefinition;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasIndexDefinitionByKeyConcept(final Class<? extends KeyConcept> keyConceptClass) {
		final SearchIndexDefinition indexDefinition = findIndexDefinitionByKeyConcept(DtObjectUtil.findDtDefinition(keyConceptClass));
		return indexDefinition != null;
	}

	private SearchIndexDefinition findIndexDefinitionByKeyConcept(final DtDefinition keyConceptDtDefinition) {
		for (final SearchIndexDefinition indexDefinition : Home.getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			if (indexDefinition.getKeyConceptDtDefinition().equals(keyConceptDtDefinition)) {
				return indexDefinition;
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void markAsDirty(final List<URI<? extends KeyConcept>> keyConceptUris) {
		Assertion.checkNotNull(keyConceptUris);
		Assertion.checkArgument(!keyConceptUris.isEmpty(), "dirty keyConceptUris cant be empty");
		//-----
		final SearchIndexDefinition searchIndexDefinition = findIndexDefinitionByKeyConcept(keyConceptUris.get(0).getDefinition());
		final List<URI<? extends KeyConcept>> dirtyElements = dirtyElementsPerIndexName.get(searchIndexDefinition.getName());
		synchronized (dirtyElements) {
			dirtyElements.addAll(keyConceptUris); //TODO : doublons ?
		}
		executorService.scheduleAtFixedRate(new ReindexTask(searchIndexDefinition, dirtyElements, this, transactionManager), 0, 5, TimeUnit.SECONDS); //une reindexation dans max 5s
	}

	/** {@inheritDoc} */
	@Override
	public void reindexAll(final SearchIndexDefinition searchIndexDefinition) {
		//TODO return un Futur ?
		executorService.scheduleAtFixedRate(new ReindexAllTask(searchIndexDefinition, this, transactionManager), 0, 5, TimeUnit.SECONDS); //une reindexation total dans max 5s
	}

}
