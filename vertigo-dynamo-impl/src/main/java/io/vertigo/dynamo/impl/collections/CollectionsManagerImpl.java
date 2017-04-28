/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.collections.IndexDtListFunctionBuilder;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.VCollectors;
import io.vertigo.dynamo.impl.collections.facet.model.FacetFactory;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListRangeFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListValueFilter;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;

/**
 * Implémentation du gestionnaire de la manipulation des collections.
 *
 * @author  pchretien
 */
public final class CollectionsManagerImpl implements CollectionsManager {
	private final Optional<IndexPlugin> indexPluginOpt;

	private final FacetFactory facetFactory;
	private final DtListProcessor listProcessor;

	/**
	 * Constructor.
	 * @param indexPluginOpt Plugin optionnel d'index
	 */
	@Inject
	public CollectionsManagerImpl(final Optional<IndexPlugin> indexPluginOpt) {
		Assertion.checkNotNull(indexPluginOpt);
		//-----
		this.indexPluginOpt = indexPluginOpt;
		facetFactory = new FacetFactory(this);
		listProcessor = new DtListProcessorImpl();
	}

	@Override
	public <D extends DtObject> DtList<D> subList(final DtList<D> dtc, final int start, final int end) {
		Assertion.checkNotNull(dtc);
		Assertion.checkArgument(start >= 0 && start <= end && end <= dtc.size(),
				"IndexOutOfBoundException, le subList n''est pas possible avec les index passés (start:{0}, end:{1}, size:{2})",
				String.valueOf(start), String.valueOf(end), String.valueOf(dtc.size())); //condition tirée de la javadoc de subList sur java.util.List
		//-----
		final DtList<D> subDtc = new DtList<>(dtc.getDefinition());
		for (int i = start; i < end; i++) {
			subDtc.add(dtc.get(i));
		}
		return subDtc;
	}

	private static StoreManager getStoreManager() {
		return Home.getApp().getComponentSpace().resolve(StoreManager.class);
	}

	@Override
	public <D extends DtObject> DtList<D> sort(final DtList<D> dtc, final String fieldName, final boolean desc) {
		Assertion.checkNotNull(dtc);
		Assertion.checkArgNotEmpty(fieldName);
		//-----
		//On crée une liste triable par l'utilitaire java.util.Collections
		final List<D> list = new ArrayList<>(dtc);

		//On trie.
		final Comparator<D> comparator = new DtObjectComparator<>(getStoreManager(), dtc.getDefinition(), fieldName, desc);
		list.sort(comparator);

		//On reconstitue la collection.
		final DtList<D> sortedDtc = new DtList<>(dtc.getDefinition());
		sortedDtc.addAll(list);
		return sortedDtc;
	}

	/** {@inheritDoc} */
	@Override
	public <R extends DtObject> FacetedQueryResult<R, DtList<R>> facetList(final DtList<R> dtList, final FacetedQuery facetedQuery) {
		Assertion.checkNotNull(dtList);
		Assertion.checkNotNull(facetedQuery);
		//-----
		//1- on applique les filtres
		final DtList<R> filteredDtList = filter(dtList, facetedQuery);
		//2- on facette
		final List<Facet> facets = facetFactory.createFacets(facetedQuery.getDefinition(), filteredDtList);

		//TODO 2a- cluster vide
		final Optional<FacetDefinition> clusterFacetDefinition = Optional.empty();
		//TODO 2b- cluster vide
		final Map<FacetValue, DtList<R>> resultCluster = Collections.emptyMap();

		//TODO 2c- mise en valeur vide
		final Map<R, Map<DtField, String>> highlights = Collections.emptyMap();

		//3- on construit le résultat
		return new FacetedQueryResult<>(Optional.of(facetedQuery), filteredDtList.size(), filteredDtList, facets, clusterFacetDefinition, resultCluster, highlights, dtList);
	}

	//=========================================================================
	//=======================Filtrage==========================================
	//=========================================================================
	private <D extends DtObject> DtList<D> filter(final DtList<D> dtList, final FacetedQuery facetedQuery) {
		final List<ListFilter> listFilters = facetedQuery.getListFilters();
		Function<DtList, DtList> filter = createDtListProcessor();
		for (final ListFilter listFilter : listFilters) {
			filter = filter.andThen(createDtListProcessor().filter(listFilter));
		}
		return filter.apply(dtList);
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor createDtListProcessor() {
		return listProcessor;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> IndexDtListFunctionBuilder<D> createIndexDtListFunctionBuilder() {
		Assertion.checkState(indexPluginOpt.isPresent(), "An IndexPlugin is required to use this function");
		//-----
		return new IndexDtListFunctionBuilderImpl<>(indexPluginOpt.get());
	}

	@Override
	public <C extends Comparable<?>, D extends DtObject> DtList<D> filterByRange(final DtList<D> list, final String fieldName, final Optional<C> min, final Optional<C> max) {
		final Predicate<D> predicate = new DtListRangeFilter<>(fieldName, min, max, true, true);
		return list.stream()
				.filter(predicate)
				.collect(VCollectors.toDtList(list.getDefinition()));
	}

	@Override
	public <D extends DtObject> DtList<D> filterByValue(final DtList<D> list, final String fieldName, final Serializable value) {
		final Predicate<D> predicate = new DtListValueFilter<>(fieldName, value);
		return list.stream()
				.filter(predicate)
				.collect(VCollectors.toDtList(list.getDefinition()));
	}
}
