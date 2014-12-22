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
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.IndexFieldNameResolver;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.dynamo.search.model.Index;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.Collection;

import javax.inject.Inject;

/**
 * Implémentation standard du gestionnaire des indexes de recherche.
 * @author dchallas
 */
public final class SearchManagerImpl implements SearchManager {
	private final Option<SearchServicesPlugin> searchServicesPlugin;

	/**
	 * Constructor.
	 * @param searchServicesPlugin Search plugin
	 */
	@Inject
	public SearchManagerImpl(final Option<SearchServicesPlugin> searchServicesPlugin) {
		Assertion.checkNotNull(searchServicesPlugin);
		//-----
		this.searchServicesPlugin = searchServicesPlugin;
	}

	private SearchServicesPlugin getSearchServices() {
		Assertion.checkArgument(searchServicesPlugin.isDefined(), "Aucun plugin de recherche déclaré");
		//-----
		return searchServicesPlugin.get();
	}

	/** {@inheritDoc} */
	@Override
	public void registerIndexFieldNameResolver(final IndexDefinition indexDefinition, final IndexFieldNameResolver indexFieldNameResolver) {
		getSearchServices().registerIndexFieldNameResolver(indexDefinition, indexFieldNameResolver);
	}

	/** {@inheritDoc} */
	@Override
	public <I extends DtObject, R extends DtObject> void putAll(final IndexDefinition indexDefinition, final Collection<Index<I, R>> indexCollection) {
		getSearchServices().putAll(indexDefinition, indexCollection);
	}

	/** {@inheritDoc} */
	@Override
	public <I extends DtObject, R extends DtObject> void put(final IndexDefinition indexDefinition, final Index<I, R> index) {
		getSearchServices().put(indexDefinition, index);
	}

	/** {@inheritDoc} */
	@Override
	public <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchQuery searchQuery, final FacetedQuery facetedQuery) {
		return getSearchServices().loadList(searchQuery, facetedQuery);
	}

	/** {@inheritDoc} */
	@Override
	public long count(final IndexDefinition indexDefinition) {
		return getSearchServices().count(indexDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final IndexDefinition indexDefinition, final URI uri) {
		getSearchServices().remove(indexDefinition, uri);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final IndexDefinition indexDefinition, final ListFilter listFilter) {
		getSearchServices().remove(indexDefinition, listFilter);
	}
}
