/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.search.elasticsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.exp.ExponentialDecayFunctionBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeBuilder;
import org.elasticsearch.search.aggregations.bucket.range.date.DateRangeBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Order;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListPatternFilterUtil;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

//vérifier
/**
 * ElasticSearch request builder from searchManager api.
 * @author pchretien, npiedeloup
 */
final class ESSearchRequestBuilder implements Builder<SearchRequestBuilder> {

	private static final boolean ACCEPT_UNMAPPED_SORT_FIELD = false; //utile uniquement pour la recherche multi type
	private static final int TERM_AGGREGATION_SIZE = 50; //max 50 facets values per facet
	private static final int TOPHITS_SUBAGGREGATION_SIZE = 10; //max 10 documents per cluster when clusterization is used
	private static final String TOPHITS_SUBAGGREGATION_NAME = "top";
	private static final String DATE_PATTERN = "dd/MM/yy";
	private static final Pattern RANGE_PATTERN = Pattern.compile("([A-Z_0-9]+):([\\[\\{])(.*) TO (.*)([\\}\\]])");

	private final SearchRequestBuilder searchRequestBuilder;
	private SearchIndexDefinition myIndexDefinition;
	private SearchQuery mySearchQuery;
	private DtListState myListState;
	private int myDefaultMaxRows = 10;

	/**
	 * @param indexName Index name (env name)
	 * @param typeName type name (dtIndex type)
	 * @param esClient ElasticSearch client
	 */
	public ESSearchRequestBuilder(final String indexName, final String typeName, final Client esClient) {
		Assertion.checkArgNotEmpty(indexName);
		Assertion.checkArgNotEmpty(typeName);
		Assertion.checkNotNull(esClient);
		//-----
		searchRequestBuilder = esClient.prepareSearch()
				.setIndices(indexName)
				.setTypes(typeName)
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.addFields(ESDocumentCodec.FULL_RESULT);
	}

	/**
	 * @param indexDefinition Index definition
	 * @return this builder
	 */
	public ESSearchRequestBuilder withSearchIndexDefinition(final SearchIndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//-----
		myIndexDefinition = indexDefinition;
		return this;
	}

	/**
	 * @param searchQuery Search query
	 * @return this builder
	 */
	public ESSearchRequestBuilder withSearchQuery(final SearchQuery searchQuery) {
		Assertion.checkNotNull(searchQuery);
		//-----
		mySearchQuery = searchQuery;
		return this;
	}

	/**
	 * @param listState List state
	 * @param defaultMaxRows default max rows
	 * @return this builder
	 */
	public ESSearchRequestBuilder withListState(final DtListState listState, final int defaultMaxRows) {
		Assertion.checkNotNull(listState);
		//-----
		myListState = listState;
		myDefaultMaxRows = defaultMaxRows;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public SearchRequestBuilder build() {
		Assertion.checkNotNull(myIndexDefinition, "You must set IndexDefinition");
		Assertion.checkNotNull(mySearchQuery, "You must set SearchQuery");
		Assertion.checkNotNull(myListState, "You must set ListState");
		//-----
		appendListState();
		appendSearchQuery(mySearchQuery, searchRequestBuilder);
		appendFacetDefinition(mySearchQuery, searchRequestBuilder);
		return searchRequestBuilder;
	}

	private void appendListState() {
		searchRequestBuilder.setFrom(myListState.getSkipRows())
				//If we send a clustering query, we don't retrieve result with hits response but with buckets
				.setSize(mySearchQuery.isClusteringFacet() ? 0 : myListState.getMaxRows().orElse(myDefaultMaxRows));
		if (myListState.getSortFieldName().isPresent()) {
			final DtField sortField = myIndexDefinition.getIndexDtDefinition().getField(myListState.getSortFieldName().get());
			final FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sortField.getName())
					.order(myListState.isSortDesc().get() ? SortOrder.DESC : SortOrder.ASC);

			if (ACCEPT_UNMAPPED_SORT_FIELD) {
				//Code désactivé pour l'instant, peut-être utile pour des recherches multi-type
				final Optional<IndexType> indexType = IndexType.readIndexType(sortField.getDomain());
				final String sortType = indexType.isPresent() ? indexType.get().getIndexDataType() : sortField.getDomain().getDataType().name();
				sortBuilder.unmappedType(sortType);
			}

			searchRequestBuilder.addSort(sortBuilder);
		}
	}

	private static void appendSearchQuery(final SearchQuery searchQuery, final SearchRequestBuilder searchRequestBuilder) {
		final BoolQueryBuilder mainBoolQueryBuilder = QueryBuilders.boolQuery();

		//on ajoute les critères de la recherche AVEC impact sur le score
		final QueryBuilder queryBuilder = translateToQueryBuilder(searchQuery.getListFilter());
		mainBoolQueryBuilder.must(queryBuilder);

		//on ajoute les filtres de sécurité SANS impact sur le score
		if (searchQuery.getSecurityListFilter().isPresent()) {
			final QueryBuilder securityFilterBuilder = translateToQueryBuilder(searchQuery.getSecurityListFilter().get());
			mainBoolQueryBuilder.filter(securityFilterBuilder);
			//use filteredQuery instead of PostFilter in order to filter aggregations too.
		}

		//on ajoute les filtres des facettes SANS impact sur le score
		if (searchQuery.getFacetedQuery().isPresent() && !searchQuery.getFacetedQuery().get().getListFilters().isEmpty()) {
			for (final ListFilter facetQuery : searchQuery.getFacetedQuery().get().getListFilters()) {
				mainBoolQueryBuilder.filter(translateToQueryBuilder(facetQuery));
			}
			//use filteredQuery instead of PostFilter in order to filter aggregations too.
		}

		final QueryBuilder requestQueryBuilder;
		if (searchQuery.isBoostMostRecent()) {
			requestQueryBuilder = appendBoostMostRecent(searchQuery, queryBuilder);
		} else {
			requestQueryBuilder = mainBoolQueryBuilder;
		}
		searchRequestBuilder
				.setQuery(requestQueryBuilder)
				//.setHighlighterFilter(true) //We don't highlight the security filter
				.setHighlighterNumOfFragments(3)
				.addHighlightedField("*");
	}

	private static QueryBuilder appendBoostMostRecent(final SearchQuery searchQuery, final QueryBuilder queryBuilder) {
		return QueryBuilders.functionScoreQuery(queryBuilder, new ExponentialDecayFunctionBuilder(searchQuery.getBoostedDocumentDateField(), null, searchQuery.getNumDaysOfBoostRefDocument() + "d").setDecay(searchQuery.getMostRecentBoost() - 1D));
	}

	private static void appendFacetDefinition(final SearchQuery searchQuery, final SearchRequestBuilder searchRequestBuilder) {
		Assertion.checkNotNull(searchRequestBuilder);
		//-----
		//On ajoute le cluster, si présent
		if (searchQuery.isClusteringFacet()) { //si il y a un cluster on le place en premier
			final FacetDefinition clusteringFacetDefinition = searchQuery.getClusteringFacetDefinition();

			final AggregationBuilder<?> aggregationBuilder = facetToAggregationBuilder(clusteringFacetDefinition);
			aggregationBuilder.subAggregation(
					AggregationBuilders.topHits(TOPHITS_SUBAGGREGATION_NAME)
							.setSize(TOPHITS_SUBAGGREGATION_SIZE)
							.setHighlighterNumOfFragments(3)
							.addHighlightedField("*"));
			//We fetch source, because it's our only source to create result list
			searchRequestBuilder.addAggregation(aggregationBuilder);
		}
		//Puis les facettes liées à la query, si présent
		if (searchQuery.getFacetedQuery().isPresent()) {
			final FacetedQueryDefinition facetedQueryDefinition = searchQuery.getFacetedQuery().get().getDefinition();
			final Collection<FacetDefinition> facetDefinitions = new ArrayList<>(facetedQueryDefinition.getFacetDefinitions());
			if (searchQuery.isClusteringFacet() && facetDefinitions.contains(searchQuery.getClusteringFacetDefinition())) {
				facetDefinitions.remove(searchQuery.getClusteringFacetDefinition());
			}
			for (final FacetDefinition facetDefinition : facetDefinitions) {
				final AggregationBuilder<?> aggregationBuilder = facetToAggregationBuilder(facetDefinition);
				searchRequestBuilder.addAggregation(aggregationBuilder);
			}
		}
	}

	private static AggregationBuilder<?> facetToAggregationBuilder(final FacetDefinition facetDefinition) {
		//Récupération des noms des champs correspondant aux facettes.
		final DtField dtField = facetDefinition.getDtField();
		if (facetDefinition.isRangeFacet()) {
			return rangeFacetToAggregationBuilder(facetDefinition, dtField);
		}
		//facette par field
		final Order facetOrder;
		switch (facetDefinition.getOrder()) {
			case alpha:
				facetOrder = Terms.Order.term(true);
				break;
			case count:
				facetOrder = Terms.Order.count(false);
				break;
			case definition:
				facetOrder = null; //ES accept null for no sorting
				break;
			default:
				throw new IllegalArgumentException("Unknown facetOrder :" + facetDefinition.getOrder());

		}

		//Warning term aggregations are inaccurate : see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-terms-aggregation.html
		return AggregationBuilders.terms(facetDefinition.getName())
				.size(TERM_AGGREGATION_SIZE)
				.field(dtField.getName())
				.order(facetOrder);
	}

	private static AggregationBuilder<?> rangeFacetToAggregationBuilder(final FacetDefinition facetDefinition, final DtField dtField) {
		//facette par range
		final DataType dataType = dtField.getDomain().getDataType();
		if (dataType == DataType.Date) {
			return dateRangeFacetToAggregationBuilder(facetDefinition, dtField);
		} else if (dataType.isNumber()) {
			return numberRangeFacetToAggregationBuilder(facetDefinition, dtField);
		}

		final FiltersAggregationBuilder filters = AggregationBuilders.filters(facetDefinition.getName());
		for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
			final String filterValue = facetRange.getListFilter().getFilterValue();
			Assertion.checkState(filterValue.contains(dtField.getName()), "RangeFilter query ({1}) should use defined fieldName {0}", dtField.getName(), filterValue);
			filters.filter(filterValue, QueryBuilders.queryStringQuery(filterValue));
		}
		return filters;
	}

	private static AggregationBuilder<RangeBuilder> numberRangeFacetToAggregationBuilder(final FacetDefinition facetDefinition, final DtField dtField) {
		final RangeBuilder rangeBuilder = AggregationBuilders.range(facetDefinition.getName())//
				.field(dtField.getName());
		for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
			final String filterValue = facetRange.getListFilter().getFilterValue();
			Assertion.checkState(filterValue.contains(dtField.getName()), "RangeFilter query ({1}) should use defined fieldName {0}", dtField.getName(), filterValue);
			final String[] parsedFilter = DtListPatternFilterUtil.parseFilter(filterValue, RANGE_PATTERN).get();
			final Optional<Double> minValue = convertToDouble(parsedFilter[3]);
			final Optional<Double> maxValue = convertToDouble(parsedFilter[4]);
			if (!minValue.isPresent()) {
				rangeBuilder.addUnboundedTo(filterValue, maxValue.get());
			} else if (!maxValue.isPresent()) {
				rangeBuilder.addUnboundedFrom(filterValue, minValue.get());
			} else {
				rangeBuilder.addRange(filterValue, minValue.get(), maxValue.get()); //always min include and max exclude in ElasticSearch
			}
		}
		return rangeBuilder;
	}

	private static AggregationBuilder<DateRangeBuilder> dateRangeFacetToAggregationBuilder(final FacetDefinition facetDefinition, final DtField dtField) {
		final DateRangeBuilder dateRangeBuilder = AggregationBuilders.dateRange(facetDefinition.getName())
				.field(dtField.getName())
				.format(DATE_PATTERN);
		for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
			final String filterValue = facetRange.getListFilter().getFilterValue();
			Assertion.checkState(filterValue.contains(dtField.getName()), "RangeFilter query ({1}) should use defined fieldName {0}", dtField.getName(), filterValue);
			final String[] parsedFilter = DtListPatternFilterUtil.parseFilter(filterValue, RANGE_PATTERN).get();
			final String minValue = parsedFilter[3];
			final String maxValue = parsedFilter[4];
			if ("*".equals(minValue)) {
				dateRangeBuilder.addUnboundedTo(filterValue, maxValue);
			} else if ("*".equals(maxValue)) {
				dateRangeBuilder.addUnboundedFrom(filterValue, minValue);
			} else {
				dateRangeBuilder.addRange(filterValue, minValue, maxValue); //always min include and max exclude in ElasticSearch
			}
		}
		return dateRangeBuilder;
	}

	private static Optional<Double> convertToDouble(final String valueToConvert) {
		final String stringValue = valueToConvert.trim();
		if ("*".equals(stringValue) || "".equals(stringValue)) {
			return Optional.empty();//pas de test
		}
		//--
		final Double result = Double.valueOf(stringValue);
		return Optional.of(result);
	}

	/**
	 * @param listFilter ListFilter
	 * @return QueryBuilder
	 */
	static QueryBuilder translateToQueryBuilder(final ListFilter listFilter) {
		Assertion.checkNotNull(listFilter);
		//-----
		final String listFilterString = cleanUserFilter(listFilter.getFilterValue());
		final String query = new StringBuilder()
				.append(" +(")
				.append(listFilterString)
				.append(')')
				.toString();
		return QueryBuilders.queryStringQuery(query)
				.lowercaseExpandedTerms(false)
				.analyzeWildcard(true);
	}

	private static String cleanUserFilter(final String filterValue) {
		return filterValue;
		//replaceAll "(?i)((?<=\\S\\s)(or|and)(?=\\s\\S))"
		//replaceAll "(?i)((?<=\\s)(or|and)(?=\\s))"
	}

}
