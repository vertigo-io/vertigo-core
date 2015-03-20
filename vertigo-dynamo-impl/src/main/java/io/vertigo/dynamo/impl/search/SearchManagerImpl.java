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

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.SearchIndexFieldNameResolver;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Assertion;

import java.util.Collection;

import javax.inject.Inject;

/**
 * Implémentation standard du gestionnaire des indexes de recherche.
 * @author dchallas
 */
public final class SearchManagerImpl implements SearchManager {
	private final SearchServicesPlugin searchServicesPlugin;

	/**
	 * Constructor.
	 * @param searchServicesPlugin Search plugin
	 */
	@Inject
	public SearchManagerImpl(final SearchServicesPlugin searchServicesPlugin) {
		Assertion.checkNotNull(searchServicesPlugin);
		//-----
		this.searchServicesPlugin = searchServicesPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public void registerIndexFieldNameResolver(final SearchIndexDefinition indexDefinition, final SearchIndexFieldNameResolver indexFieldNameResolver) {
		searchServicesPlugin.registerIndexFieldNameResolver(indexDefinition, indexFieldNameResolver);
	}

	/** {@inheritDoc} */
	@Override
	public <I extends DtObject, R extends DtObject> void putAll(final SearchIndexDefinition indexDefinition, final Collection<SearchIndex<I, R>> indexCollection) {
		searchServicesPlugin.putAll(indexDefinition, indexCollection);
	}

	/** {@inheritDoc} */
	@Override
	public <I extends DtObject, R extends DtObject> void put(final SearchIndexDefinition indexDefinition, final SearchIndex<I, R> index) {
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
}
