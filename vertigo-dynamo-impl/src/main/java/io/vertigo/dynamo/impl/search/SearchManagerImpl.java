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
import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
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
	private final SearchServicesPlugin searchServicesPlugin;

	private final ScheduledExecutorService executorService; //TODO : replace by WorkManager to make distributed work easier
	private final Map<String, List<URI<? extends DtSubject>>> dirtyElementsPerIndexName = new HashMap<>();

	/**
	 * Constructor.
	 * @param searchServicesPlugin Search plugin
	 */
	@Inject
	public SearchManagerImpl(final SearchServicesPlugin searchServicesPlugin) {
		Assertion.checkNotNull(searchServicesPlugin);
		//-----
		this.searchServicesPlugin = searchServicesPlugin;
		executorService = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public void start() {
		for (final SearchIndexDefinition indexDefinition : Home.getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			dirtyElementsPerIndexName.put(indexDefinition.getName(), new ArrayList<URI<? extends DtSubject>>());
		}
	}

	@Override
	public void stop() {
		//nothing
	}

	/** {@inheritDoc} */
	@Override
	public <S extends DtSubject, I extends DtObject, R extends DtObject> void putAll(final SearchIndexDefinition indexDefinition, final Collection<SearchIndex<S, I, R>> indexCollection) {
		searchServicesPlugin.putAll(indexDefinition, indexCollection);
	}

	/** {@inheritDoc} */
	@Override
	public <S extends DtSubject, I extends DtObject, R extends DtObject> void put(final SearchIndexDefinition indexDefinition, final SearchIndex<S, I, R> index) {
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
	public void remove(final SearchIndexDefinition indexDefinition, final URI uri) {
		searchServicesPlugin.remove(indexDefinition, uri);
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll(final SearchIndexDefinition indexDefinition, final ListFilter listFilter) {
		searchServicesPlugin.remove(indexDefinition, listFilter);
	}

	/** {@inheritDoc} */
	@Override
	public SearchIndexDefinition findIndexDefinitionBySubject(final Class<? extends DtSubject> dtSubjectClass) {
		final SearchIndexDefinition indexDefinition = findIndexDefinitionBySubject(DtObjectUtil.findDtDefinition(dtSubjectClass));
		Assertion.checkNotNull(indexDefinition, "No SearchIndexDefinition was defined for this Subject : {0}", dtSubjectClass.getSimpleName());
		return indexDefinition;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasIndexDefinitionBySubject(final Class<? extends DtSubject> dtSubjectClass) {
		final SearchIndexDefinition indexDefinition = findIndexDefinitionBySubject(DtObjectUtil.findDtDefinition(dtSubjectClass));
		return indexDefinition != null;
	}

	private SearchIndexDefinition findIndexDefinitionBySubject(final DtDefinition subjectDtDefinition) {
		for (final SearchIndexDefinition indexDefinition : Home.getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			if (indexDefinition.getSubjectDtDefinition().equals(subjectDtDefinition)) {
				return indexDefinition;
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void markAsDirty(final List<URI<? extends DtSubject>> subjectUris) {
		Assertion.checkNotNull(subjectUris);
		Assertion.checkArgument(!subjectUris.isEmpty(), "dirty subjectUris cant be empty");
		//-----
		final SearchIndexDefinition searchIndexDefinition = findIndexDefinitionBySubject(subjectUris.get(0).getDefinition());
		final List<URI<? extends DtSubject>> dirtyElements = dirtyElementsPerIndexName.get(searchIndexDefinition.getName());
		synchronized (dirtyElements) {
			dirtyElements.addAll(subjectUris); //TODO : doublons ?
		}
		executorService.scheduleAtFixedRate(new ReindexTask(searchIndexDefinition, dirtyElements, this), 0, 5, TimeUnit.SECONDS); //une reindexation dans max 5s
	}

	/** {@inheritDoc} */
	@Override
	public void reindexAll(final SearchIndexDefinition searchIndexDefinition) {
		//TODO return un Futur ?
		executorService.scheduleAtFixedRate(new ReindexAllTask(searchIndexDefinition, this), 0, 5, TimeUnit.SECONDS); //une reindexation total dans max 5s
	}

}
