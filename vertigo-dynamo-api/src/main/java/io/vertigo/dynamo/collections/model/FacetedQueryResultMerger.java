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
package io.vertigo.dynamo.collections.model;

import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.MessageKey;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Search faceted result merger.
 * First element is use for source, the empty result list's type (result's elements are in cluster not in list)
 * Build a cluster in result for each merged list.
 * List are merge if use the same filter when merging, in this case they must use the same type.
 *
 * @author npiedeloup
 * @param <R> Result object's type
 * @param <S> Source object's type
 */
public final class FacetedQueryResultMerger<R extends DtObject, S> implements Builder<FacetedQueryResult<R, S>> {

	private final Map<String, FacetValue> facetValuePerFilter = new HashMap<>();
	private final Map<FacetValue, List<FacetedQueryResult<?, S>>> otherResults = new HashMap<>();

	private Option<FacetDefinition> clusterFacetDefinition = Option.<FacetDefinition> none();
	private final Option<FacetedQuery> facetedQuery;
	private final DtList<R> results;
	private final S source;

	/**
	 * Constructeur.
	 * @param firstResult First facetedQueryResult used for facet, source and result list's type
	 * @param firstResultFilter SearchQuery filter for first result
	 * @param firstResultLabel Default string label for first result
	 * @param firstResultLabelKey MessageKey label for first result
	 */
	public FacetedQueryResultMerger(final FacetedQueryResult<?, S> firstResult, final String firstResultFilter, final String firstResultLabel, final MessageKey firstResultLabelKey) {
		Assertion.checkNotNull(firstResult);
		Assertion.checkArgNotEmpty(firstResultFilter);
		Assertion.checkArgument(firstResultLabelKey != null || firstResultLabel != null, "You must set a label for firstResult when merging result");
		//-----
		//On garde les infos qui sont basés sur le premier élément
		facetedQuery = firstResult.getFacetedQuery();
		results = new DtList(firstResult.getDtList().getDefinition()); //faux : le type de la liste est incorrect, mais heureusement elle est vide.
		source = firstResult.getSource();

		with(firstResult, firstResultFilter, firstResultLabel, firstResultLabelKey);
	}

	/**
	 * Merger should create a facet for this cluster.
	 * @param result Result to merge
	 * @param resultFilter SearchQuery filter for result
	 * @param resultLabel Default string label for result
	 * @param resultLabelKey MessageKey label for result
	 * @return this builder
	 */
	public FacetedQueryResultMerger<R, S> with(final FacetedQueryResult<?, S> result, final String resultFilter, final String resultLabel, final MessageKey resultLabelKey) {
		Assertion.checkNotNull(result);
		Assertion.checkArgNotEmpty(resultFilter);
		Assertion.checkArgument(resultLabelKey != null || resultLabel != null, "You must set a label when merging result");
		//-----
		FacetValue otherFacetValue = facetValuePerFilter.get(resultFilter);
		if (otherFacetValue == null) {
			otherFacetValue = new FacetValue(new ListFilter(resultFilter), new MessageText(resultLabel, resultLabelKey));
			facetValuePerFilter.put(resultFilter, otherFacetValue);
		}

		List<FacetedQueryResult<?, S>> facetedQueryResults = otherResults.get(otherFacetValue);
		if (facetedQueryResults == null) {
			facetedQueryResults = new ArrayList<>();
			otherResults.put(otherFacetValue, facetedQueryResults);
		}
		facetedQueryResults.add(result);
		return this;
	}

	/**
	 * Merger should create a facet for this cluster.
	 * @param facetDefinitionName FacetDefinitionName
	 * @return this builder
	 */
	public FacetedQueryResultMerger<R, S> withFacet(final String facetDefinitionName) {
		Assertion.checkArgNotEmpty(facetDefinitionName);
		//-----
		final FacetDefinition facetDefinition = Home.getDefinitionSpace().resolve(facetDefinitionName, FacetDefinition.class);
		//Très compliqué de créer une fausse FacetDefinition, il est plus simple d'imposer une facet détournée.
		clusterFacetDefinition = Option.some(facetDefinition);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public FacetedQueryResult<R, S> build() {
		Assertion.checkArgument(otherResults.size() > 1, "You need at least 2 FacetedQueryResults in order to merge them");
		//-----
		long totalCount = 0;
		final Map<FacetValue, DtList<R>> clustersDtc = new HashMap<>(otherResults.size());
		final Map<FacetValue, Long> clustersCount = new HashMap<>(otherResults.size());
		final List<Facet> facets = new ArrayList<>();
		final Map<R, Map<DtField, String>> highlights = Collections.emptyMap();

		for (final Entry<FacetValue, List<FacetedQueryResult<?, S>>> otherResult : otherResults.entrySet()) {
			long clusterCount = 0;
			//merge count
			for (final FacetedQueryResult<?, S> result : otherResult.getValue()) {
				clusterCount += result.getCount();
			}
			//cluster result
			final DtList clusterDtList;
			if (otherResult.getValue().size() == 1) {
				clusterDtList = otherResult.getValue().get(0).getDtList();
			} else {
				clusterDtList = new DtList(otherResult.getValue().get(0).getDtList().getDefinition());
				for (final FacetedQueryResult<?, S> result : otherResult.getValue()) {
					clusterDtList.addAll(result.getDtList());
				}
			}
			clustersDtc.put(otherResult.getKey(), clusterDtList);
			clustersCount.put(otherResult.getKey(), clusterCount);
			totalCount += clusterCount;
			//TODO merge facets
			//TODO merge highlights
		}

		if (clusterFacetDefinition.isDefined()) {
			final Facet clusterFacet = new Facet(clusterFacetDefinition.get(), clustersCount);
			facets.add(clusterFacet);
		}
		return new FacetedQueryResult<>(facetedQuery, totalCount, results, facets, clustersDtc, highlights, source);
	}
}
