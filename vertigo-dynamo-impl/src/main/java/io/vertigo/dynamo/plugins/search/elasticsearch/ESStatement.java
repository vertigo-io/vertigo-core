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
package io.vertigo.dynamo.plugins.search.elasticsearch;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListPatternFilterUtil;
import io.vertigo.dynamo.search.SearchIndexFieldNameResolver;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.exp.ExponentialDecayFunctionBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeBuilder;
import org.elasticsearch.search.aggregations.bucket.range.date.DateRangeBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

//vérifier
/**
 * Requête physique d'accès à ElasticSearch.
 * Le driver exécute les requêtes de façon synchrone dans le contexte transactionnelle de la ressource.
 * @author pchretien, npiedeloup
 * @param <I> Type de l'objet contenant les champs à indexer
 * @param <R> Type de l'objet resultant de la recherche
 */
final class ESStatement<I extends DtObject, R extends DtObject> {
	private static final int TOPHITS_SUBAGGREAGTION_SIZE = 20; //max 20 documents per cluster when cluserization is used
	private static final String TOPHITS_SUBAGGREAGTION_NAME = "top";
	private static final String DATE_PATTERN = "dd/MM/yy";
	private static final Pattern RANGE_PATTERN = Pattern.compile("([A-Z_0-9]+):([\\[\\]])(.*) TO (.*)([\\[\\]])");

	private final String indexName;
	private final Client esClient;
	private final ESDocumentCodec elasticSearchDocumentCodec;
	private final SearchIndexFieldNameResolver indexFieldNameResolver;

	/**
	 * Constructeur.
	 * @param solrDocumentCodec Codec de traduction (bi-directionnelle) des objets métiers en document Solr
	 * @param esClient Client ElasticSearch.
	 * @param indexFieldNameResolver Resolver de nom de champ de l'index
	 */
	ESStatement(final ESDocumentCodec solrDocumentCodec, final String indexName, final Client esClient, final SearchIndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkArgNotEmpty(indexName);
		Assertion.checkNotNull(solrDocumentCodec);
		Assertion.checkNotNull(esClient);
		Assertion.checkNotNull(indexFieldNameResolver);
		//-----
		this.indexName = indexName;
		this.esClient = esClient;
		this.elasticSearchDocumentCodec = solrDocumentCodec;
		this.indexFieldNameResolver = indexFieldNameResolver;
	}

	/**
	 * @param indexCollection Collection des indexes à insérer
	 */
	void putAll(final Collection<SearchIndex<I, R>> indexCollection) {
		//Injection spécifique au moteur d'indexation.
		try {
			final BulkRequestBuilder bulkRequest = esClient.prepareBulk();
			for (final SearchIndex<I, R> index : indexCollection) {
				try (final XContentBuilder xContentBuilder = elasticSearchDocumentCodec.index2XContentBuilder(index, indexFieldNameResolver)) {
					bulkRequest.add(esClient.prepareIndex(indexName, index.getURI().getDefinition().getName(), index.getURI().toURN())
							.setSource(xContentBuilder));
				}
			}
			final BulkResponse bulkResponse = bulkRequest.execute().actionGet();
			if (bulkResponse.hasFailures()) {
				// process failures by iterating through each bulk response item
			}
		} catch (final IOException e) {
			handleIOException(e);
		}
	}

	private static void handleIOException(final IOException e) {
		throw new RuntimeException("Serveur ElasticSearch indisponible", e);
	}

	/**
	 * @param index index à insérer
	 */
	void put(final SearchIndex<I, R> index) {
		//Injection spécifique au moteur d'indexation.
		try (final XContentBuilder xContentBuilder = elasticSearchDocumentCodec.index2XContentBuilder(index, indexFieldNameResolver)) {
			esClient.prepareIndex(indexName, index.getURI().getDefinition().getName(), index.getURI().toURN())
					.setSource(xContentBuilder)
					.execute() //execute asynchrone
					.actionGet(); //get wait exec
		} catch (final IOException e) {
			handleIOException(e);
		}
	}

	/**
	 * Supprime des documents.
	 * @param indexDefinition Index concerné
	 * @param query Requete de filtrage des documents à supprimer
	 */
	void remove(final SearchIndexDefinition indexDefinition, final ListFilter query) {
		Assertion.checkNotNull(query);
		//-----
		final QueryBuilder queryBuilder = translateToQueryBuilder(query, indexFieldNameResolver);
		esClient.prepareDeleteByQuery(indexName)
				.setQuery(queryBuilder)
				.execute()
				.actionGet();
	}

	/**
	 * Supprime un document.
	 * @param indexDefinition Index concerné
	 * @param uri Uri du document à supprimer
	 */
	void remove(final SearchIndexDefinition indexDefinition, final URI uri) {
		Assertion.checkNotNull(uri);
		//-----
		esClient.prepareDelete(indexName, uri.getDefinition().getName(), uri.toURN())
				.execute()
				.actionGet();
	}

	/**
	 * @param indexDefinition Index de recherche
	 * @param searchQuery Mots clés de recherche
	 * @param listState Etat de la liste (tri et pagination)
	 * @param defaultMaxRows Nombre de ligne max par defaut
	 * @return Résultat de la recherche
	 */
	FacetedQueryResult<R, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState, final int defaultMaxRows) {
		Assertion.checkNotNull(searchQuery);
		//-----
		final SearchRequestBuilder searchRequestBuilder = createSearchRequestBuilder(indexDefinition, searchQuery, listState, defaultMaxRows);
		appendFacetDefinition(searchQuery, searchRequestBuilder);

		final SearchResponse queryResponse = searchRequestBuilder.execute().actionGet();
		return translateQuery(indexDefinition, queryResponse, searchQuery);
	}

	/**
	 * @return Nombre de document indexés
	 */
	public long count() {
		final CountResponse response = esClient.prepareCount(indexName)
				.execute()
				.actionGet();
		return response.getCount();
	}

	private SearchRequestBuilder createSearchRequestBuilder(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState, final int defaultMaxRows) {
		final SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(indexName)
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.addFields(ESDocumentCodec.FULL_RESULT)
				.setFrom(listState.getSkipRows())
				.setSize(listState.getMaxRows().getOrElse(defaultMaxRows));
		if (listState.getSortFieldName().isDefined()) {
			final DtField sortField = indexDefinition.getIndexDtDefinition().getField(listState.getSortFieldName().get());
			final FieldSortBuilder sortBuilder = SortBuilders.fieldSort(indexFieldNameResolver.obtainIndexFieldName(sortField))
					.ignoreUnmapped(true)
					.order(listState.isSortDesc().get() ? SortOrder.DESC : SortOrder.ASC);
			searchRequestBuilder.addSort(sortBuilder);
		}
		QueryBuilder queryBuilder = translateToQueryBuilder(searchQuery.getListFilter(), indexFieldNameResolver);
		if (searchQuery.isBoostMostRecent()) {
			queryBuilder = appendBoostMostRecent(searchQuery, queryBuilder);
		}
		if (searchQuery.getFacetedQuery().isDefined() && !searchQuery.getFacetedQuery().get().getListFilters().isEmpty()) {
			final AndFilterBuilder filterBuilder = FilterBuilders.andFilter();
			for (final ListFilter facetQuery : searchQuery.getFacetedQuery().get().getListFilters()) {
				filterBuilder.add(translateToFilterBuilder(facetQuery, indexFieldNameResolver));
			}
			//use filteredQuery instead of PostFilter in order to filter aggregations too.
			queryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBuilder);
		}
		return searchRequestBuilder
				.setQuery(queryBuilder)
				.setHighlighterFilter(true)
				.setHighlighterNumOfFragments(3)
				.addHighlightedField("*");
	}

	private static QueryBuilder appendBoostMostRecent(final SearchQuery searchQuery, final QueryBuilder queryBuilder) {
		return QueryBuilders.functionScoreQuery(queryBuilder, new ExponentialDecayFunctionBuilder(searchQuery.getBoostedDocumentDateField(), null, searchQuery.getNumDaysOfBoostRefDocument() + "d").setDecay(searchQuery.getMostRecentBoost() - 1d));
	}

	private void appendFacetDefinition(final SearchQuery searchQuery, final SearchRequestBuilder searchRequestBuilder) {
		Assertion.checkNotNull(searchRequestBuilder);
		//-----
		//On ajoute le cluster, si présent
		if (searchQuery.isClusteringFacet()) { //si il y a un cluster on le place en premier
			final FacetDefinition clusteringFacetDefinition = searchQuery.getClusteringFacetDefinition();

			final AggregationBuilder aggregationBuilder = facetToAggregationBuilder(clusteringFacetDefinition);
			aggregationBuilder.subAggregation(
					AggregationBuilders.topHits(TOPHITS_SUBAGGREAGTION_NAME)
							.setSize(TOPHITS_SUBAGGREAGTION_SIZE)
							.setFetchSource(false));
			searchRequestBuilder.addAggregation(aggregationBuilder);
		}
		//Puis les facettes liées à la query, si présent
		if (searchQuery.getFacetedQuery().isDefined()) {
			final FacetedQueryDefinition facetedQueryDefinition = searchQuery.getFacetedQuery().get().getDefinition();
			final Collection<FacetDefinition> facetDefinitions = new ArrayList<>(facetedQueryDefinition.getFacetDefinitions());
			if (searchQuery.isClusteringFacet() && facetDefinitions.contains(searchQuery.getClusteringFacetDefinition())) {
				facetDefinitions.remove(searchQuery.getClusteringFacetDefinition());
			}
			for (final FacetDefinition facetDefinition : facetDefinitions) {
				final AggregationBuilder aggregationBuilder = facetToAggregationBuilder(facetDefinition);
				searchRequestBuilder.addAggregation(aggregationBuilder);
			}
		}
	}

	private AggregationBuilder facetToAggregationBuilder(final FacetDefinition facetDefinition) {
		//Récupération des noms des champs correspondant aux facettes.
		final DtField dtField = facetDefinition.getDtField();
		if (facetDefinition.isRangeFacet()) {
			//facette par range
			//solrQuery.addFacetQuery(translateToQueryBuilder(facetRange.getListFilter(), indexFieldNameResolver));
			final DataType dataType = dtField.getDomain().getDataType();
			final DateRangeBuilder dateRangeBuilder;
			if (dataType == DataType.Date) {
				dateRangeBuilder = AggregationBuilders.dateRange(facetDefinition.getName())
						.field(indexFieldNameResolver.obtainIndexFieldName(dtField))
						.format(DATE_PATTERN);
				for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
					final String filterValue = facetRange.getListFilter().getFilterValue();
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

			final RangeBuilder rangeBuilder = AggregationBuilders.range(facetDefinition.getName())//
					.field(indexFieldNameResolver.obtainIndexFieldName(dtField));
			for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
				final String filterValue = facetRange.getListFilter().getFilterValue();
				final String[] parsedFilter = DtListPatternFilterUtil.parseFilter(filterValue, RANGE_PATTERN).get();
				final Option<Double> minValue = convertToDouble(parsedFilter[3]);
				final Option<Double> maxValue = convertToDouble(parsedFilter[4]);
				if (minValue.isEmpty()) {
					rangeBuilder.addUnboundedTo(filterValue, maxValue.get());
				} else if (maxValue.isEmpty()) {
					rangeBuilder.addUnboundedFrom(filterValue, minValue.get());
				} else {
					rangeBuilder.addRange(filterValue, minValue.get(), maxValue.get()); //always min include and max exclude in ElasticSearch
				}
			}
			return rangeBuilder;
		}
		//facette par field
		final TermsBuilder aggregationBuilder = AggregationBuilders.terms(facetDefinition.getName())
				.size(50) //Warning term aggregations are inaccurate : see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-terms-aggregation.html
				.field(indexFieldNameResolver.obtainIndexFieldName(dtField));
		return aggregationBuilder;
	}

	private static Option<Double> convertToDouble(final String valueToConvert) {
		final String stringValue = valueToConvert.trim();
		if ("*".equals(stringValue) || "".equals(stringValue)) {
			return Option.none();//pas de test
		}
		//--
		final Double result = Double.valueOf(stringValue);
		return Option.some(result);
	}

	private static QueryBuilder translateToQueryBuilder(final ListFilter listFilter, final SearchIndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkNotNull(listFilter);
		//-----
		final String query = new StringBuilder()
				.append(" +(")
				.append(listFilter.getFilterValue())
				.append(')')
				.toString();
		return QueryBuilders.queryString(indexFieldNameResolver.replaceAllIndexFieldNames(query))
				.lowercaseExpandedTerms(false);
	}

	private static FilterBuilder translateToFilterBuilder(final ListFilter query, final SearchIndexFieldNameResolver indexFieldNameResolver) {
		return FilterBuilders.queryFilter(translateToQueryBuilder(query, indexFieldNameResolver));
	}

	private FacetedQueryResult<R, SearchQuery> translateQuery(final SearchIndexDefinition indexDefinition, final SearchResponse queryResponse, final SearchQuery searchQuery) {

		final Map<R, Map<DtField, String>> resultHighlights = new HashMap<>();
		final DtList<R> dtc = new DtList<>(indexDefinition.getResultDtDefinition());
		final Map<String, R> dtcIndex = new HashMap<>();
		for (final SearchHit searchHit : queryResponse.getHits()) {
			final SearchIndex<I, R> index = elasticSearchDocumentCodec.searchHit2Index(indexDefinition, searchHit);
			final R result = index.getResultDtObject();
			dtc.add(result);
			dtcIndex.put(searchHit.getId(), result);
			final Map<DtField, String> highlights = createHighlight(searchHit, indexDefinition.getIndexDtDefinition(), indexFieldNameResolver);
			resultHighlights.put(result, highlights);
		}
		final Map<FacetValue, DtList<R>> resultCluster = createCluster(indexDefinition, searchQuery, queryResponse, dtcIndex);
		//On fabrique à la volée le résultat.
		final List<Facet> facets = createFacetList(searchQuery, queryResponse);
		final long count = queryResponse.getHits().getTotalHits();
		return new FacetedQueryResult<>(searchQuery.getFacetedQuery(), count, dtc, facets, resultCluster, resultHighlights, searchQuery);
	}

	private Map<FacetValue, DtList<R>> createCluster(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final SearchResponse queryResponse, final Map<String, R> dtcIndex) {

		final Map<FacetValue, DtList<R>> resultCluster = new LinkedHashMap<>();
		if (searchQuery.isClusteringFacet()) {
			final FacetDefinition facetDefinition = searchQuery.getClusteringFacetDefinition();
			final Aggregation facetAggregation = queryResponse.getAggregations().get(facetDefinition.getName());
			if (facetDefinition.isRangeFacet()) {
				//Cas des facettes par 'range'
				final Range rangeBuckets = (Range) facetAggregation;
				for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
					final Bucket value = rangeBuckets.getBucketByKey(facetRange.getListFilter().getFilterValue());
					final SearchHits facetSearchHits = ((TopHits) value.getAggregations().get(TOPHITS_SUBAGGREAGTION_NAME)).getHits();
					final DtList<R> facetDtc = new DtList<>(indexDefinition.getResultDtDefinition());
					for (final SearchHit searchHit : facetSearchHits) {
						facetDtc.add(dtcIndex.get(searchHit.getId()));
					}
					resultCluster.put(facetRange, facetDtc);
				}
			} else {
				//Cas des facettes par 'term'
				final MultiBucketsAggregation multiBuckets = (MultiBucketsAggregation) facetAggregation;
				FacetValue facetValue;
				for (final Bucket value : multiBuckets.getBuckets()) {
					final MessageText label = new MessageText(value.getKey(), null);
					final String query = facetDefinition.getDtField().name() + ":\"" + value.getKey() + "\"";
					facetValue = new FacetValue(new ListFilter(query), label);

					final SearchHits facetSearchHits = ((TopHits) value.getAggregations().get(TOPHITS_SUBAGGREAGTION_NAME)).getHits();
					final DtList<R> facetDtc = new DtList<>(indexDefinition.getResultDtDefinition());
					for (final SearchHit searchHit : facetSearchHits) {
						facetDtc.add(dtcIndex.get(searchHit.getId()));
					}
					resultCluster.put(facetValue, facetDtc);
				}
			}
		}
		return resultCluster;
	}

	private static Map<DtField, String> createHighlight(final SearchHit searchHit, final DtDefinition indexDtDefinition, final SearchIndexFieldNameResolver indexFieldNameResolver) {
		final Map<DtField, String> highlights = new HashMap<>();
		final Map<String, HighlightField> highlightsMap = searchHit.getHighlightFields();

		for (final Map.Entry<String, HighlightField> entry : highlightsMap.entrySet()) {
			if (indexDtDefinition.contains(entry.getKey())) { //TODO : may really highlighs match on FULL_RESULT field ?
				continue; //skip hightlights on unknown fields
			}
			final StringBuilder sb = new StringBuilder();
			for (final Text fragment : entry.getValue().getFragments()) {
				sb.append("<hlfrag>").append(fragment).append("</hlfrag>");
			}
			final DtField dtField = indexDtDefinition.getField(indexFieldNameResolver.obtainDtFieldName(entry.getKey()));
			highlights.put(dtField, sb.toString());
		}
		return highlights;
	}

	private static List<Facet> createFacetList(final SearchQuery searchQuery, final SearchResponse queryResponse) {
		final List<Facet> facets = new ArrayList<>();
		if (searchQuery.getFacetedQuery().isDefined() && queryResponse.getAggregations() != null) {
			final FacetedQueryDefinition queryDefinition = searchQuery.getFacetedQuery().get().getDefinition();
			for (final Aggregation aggregation : queryResponse.getAggregations().asList()) {
				final FacetDefinition facetDefinition = queryDefinition.getFacetDefinition(aggregation.getName());
				final Facet currentFacet;
				if (facetDefinition.isRangeFacet()) {
					//Cas des facettes par 'range'
					final Range rangeBuckets = (Range) aggregation;
					currentFacet = createFacetRange(facetDefinition, rangeBuckets);
				} else {
					//Cas des facettes par 'term'
					final MultiBucketsAggregation multiBuckets = (MultiBucketsAggregation) aggregation;
					currentFacet = createTermFacet(facetDefinition, multiBuckets);
				}
				facets.add(currentFacet);
			}
		}
		return facets;
	}

	private static Facet createTermFacet(final FacetDefinition facetDefinition, final MultiBucketsAggregation multiBuckets) {
		final Map<FacetValue, Long> facetValues = new HashMap<>();
		FacetValue facetValue;
		for (final Bucket value : multiBuckets.getBuckets()) {
			final MessageText label = new MessageText(value.getKey(), null);
			final String query = facetDefinition.getDtField().name() + ":\"" + value.getKey() + "\"";
			facetValue = new FacetValue(new ListFilter(query), label);
			facetValues.put(facetValue, value.getDocCount());
		}

		//tri des facettes
		final Comparator<FacetValue> facetComparator = new Comparator<FacetValue>() {
			@Override
			public int compare(final FacetValue o1, final FacetValue o2) {
				final int compareNbDoc = (int) (facetValues.get(o2) - facetValues.get(o1));
				return compareNbDoc != 0 ? compareNbDoc : o1.getLabel().getDisplay().compareToIgnoreCase(o2.getLabel().getDisplay());
			}
		};
		final Map<FacetValue, Long> sortedFacetValues = new TreeMap<>(facetComparator);
		sortedFacetValues.putAll(facetValues);

		return new Facet(facetDefinition, sortedFacetValues);
	}

	private static Facet createFacetRange(final FacetDefinition facetDefinition, final Range rangeBuckets) {
		//Cas des facettes par range
		final Map<FacetValue, Long> rangeValues = new LinkedHashMap<>();
		for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
			final Bucket value = rangeBuckets.getBucketByKey(facetRange.getListFilter().getFilterValue());
			rangeValues.put(facetRange, value.getDocCount());
		}
		return new Facet(facetDefinition, rangeValues);
	}
}
