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

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracker;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.eventbus.EventSuscriber;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.store.StoreEvent;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Implémentation standard du gestionnaire des indexes de recherche.
 * @author dchallas, npiedeloup
 */
public final class SearchManagerImpl implements SearchManager, Activeable {

	private static final String ANALYTICS_TYPE = "search";
	private final VTransactionManager transactionManager;
	private final AnalyticsManager analyticsManager;
	private final SearchServicesPlugin searchServicesPlugin;

	private final ScheduledExecutorService executorService; //TODO : replace by WorkManager to make distributed work easier
	private final Map<String, List<URI<? extends KeyConcept>>> dirtyElementsPerIndexName = new HashMap<>();

	/**
	 * Constructor.
	 * @param searchServicesPlugin Search plugin
	 * @param eventBusManager EventBus manager
	 * @param transactionManager Transaction Manager
	 * @param analyticsManager Analytics manager
	 */
	@Inject
	public SearchManagerImpl(final SearchServicesPlugin searchServicesPlugin, final EventBusManager eventBusManager, final VTransactionManager transactionManager, final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(searchServicesPlugin);
		Assertion.checkNotNull(eventBusManager);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(analyticsManager);
		//-----
		this.searchServicesPlugin = searchServicesPlugin;
		this.analyticsManager = analyticsManager;

		eventBusManager.register(this);

		executorService = Executors.newSingleThreadScheduledExecutor();
		this.transactionManager = transactionManager;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		for (final SearchIndexDefinition indexDefinition : Home.getApp().getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			dirtyElementsPerIndexName.put(indexDefinition.getName(), new ArrayList<URI<? extends KeyConcept>>());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		executorService.shutdown();
	}

	/** {@inheritDoc} */
	@Override
	public <S extends KeyConcept, I extends DtObject> void putAll(final SearchIndexDefinition indexDefinition, final Collection<SearchIndex<S, I>> indexCollection) {
		try (AnalyticsTracker tracker = analyticsManager.startTracker(ANALYTICS_TYPE, indexDefinition.getName() + "/putAll")) {
			searchServicesPlugin.putAll(indexDefinition, indexCollection);
			tracker.setMeasure("nbModifiedRow", indexCollection.size());
			tracker.markAsSucceeded();
		}
	}

	/** {@inheritDoc} */
	@Override
	public <S extends KeyConcept, I extends DtObject> void put(final SearchIndexDefinition indexDefinition, final SearchIndex<S, I> index) {
		try (AnalyticsTracker tracker = analyticsManager.startTracker(ANALYTICS_TYPE, indexDefinition.getName() + "/put")) {
			searchServicesPlugin.put(indexDefinition, index);
			tracker.setMeasure("nbModifiedRow", 1);
			tracker.markAsSucceeded();
		}
	}

	/** {@inheritDoc} */
	@Override
	public <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState) {
		try (AnalyticsTracker tracker = analyticsManager.startTracker(ANALYTICS_TYPE, indexDefinition.getName() + "/load")) {
			final FacetedQueryResult<R, SearchQuery> result = searchServicesPlugin.loadList(indexDefinition, searchQuery, listState);
			tracker.setMeasure("nbSelectedRow", result.getCount());
			tracker.markAsSucceeded();
			return result;
		}
	}

	/** {@inheritDoc} */
	@Override
	public long count(final SearchIndexDefinition indexDefinition) {
		try (AnalyticsTracker tracker = analyticsManager.startTracker(ANALYTICS_TYPE, indexDefinition.getName() + "/count")) {
			final long result = searchServicesPlugin.count(indexDefinition);
			tracker.markAsSucceeded();
			return result;
		}
	}

	/** {@inheritDoc} */
	@Override
	public <S extends KeyConcept> void remove(final SearchIndexDefinition indexDefinition, final URI<S> uri) {
		try (AnalyticsTracker tracker = analyticsManager.startTracker(ANALYTICS_TYPE, indexDefinition.getName() + "/remove")) {
			searchServicesPlugin.remove(indexDefinition, uri);
			tracker.setMeasure("nbModifiedRow", 1);
			tracker.markAsSucceeded();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll(final SearchIndexDefinition indexDefinition, final ListFilter listFilter) {
		try (AnalyticsTracker tracker = analyticsManager.startTracker(ANALYTICS_TYPE, indexDefinition.getName() + "/removeAll")) {
			searchServicesPlugin.remove(indexDefinition, listFilter);
			tracker.markAsSucceeded();
		}
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
	public boolean hasIndexDefinitionByKeyConcept(final DtDefinition keyConceptDefinition) {
		final SearchIndexDefinition indexDefinition = findIndexDefinitionByKeyConcept(keyConceptDefinition);
		return indexDefinition != null;
	}

	private static SearchIndexDefinition findIndexDefinitionByKeyConcept(final DtDefinition keyConceptDtDefinition) {
		for (final SearchIndexDefinition indexDefinition : Home.getApp().getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
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
		final DtDefinition keyConceptDefinition = keyConceptUris.get(0).getDefinition();
		final SearchIndexDefinition searchIndexDefinition = findIndexDefinitionByKeyConcept(keyConceptDefinition);
		Assertion.checkNotNull(searchIndexDefinition, "No SearchIndexDefinition was defined for this keyConcept : {0}", keyConceptDefinition.getName());
		//-----
		final List<URI<? extends KeyConcept>> dirtyElements = dirtyElementsPerIndexName.get(searchIndexDefinition.getName());
		synchronized (dirtyElements) {
			dirtyElements.addAll(keyConceptUris); //TODO : doublons ?
		}
		executorService.schedule(new ReindexTask(searchIndexDefinition, dirtyElements, this, transactionManager), 5, TimeUnit.SECONDS); //une reindexation dans max 5s
	}

	/** {@inheritDoc} */
	@Override
	public Future<Long> reindexAll(final SearchIndexDefinition searchIndexDefinition) {
		final WritableFuture<Long> reindexFuture = new WritableFuture<>();
		executorService.schedule(new ReindexAllTask(searchIndexDefinition, reindexFuture, this, transactionManager), 5, TimeUnit.SECONDS); //une reindexation total dans max 5s
		return reindexFuture;
	}

	/**
	 * Receive Store event.
	 * @param storeEvent Store event
	 */
	@EventSuscriber
	public void onEvent(final StoreEvent storeEvent) {
		final URI uri = storeEvent.getUri();
		//On ne traite l'event que si il porte sur un KeyConcept
		if (uri.getDefinition().getStereotype() == DtStereotype.KeyConcept
				&& hasIndexDefinitionByKeyConcept(uri.getDefinition())) {
			final List<URI<? extends KeyConcept>> list = Collections.<URI<? extends KeyConcept>> singletonList(uri);
			markAsDirty(list);
		}
	}

}
