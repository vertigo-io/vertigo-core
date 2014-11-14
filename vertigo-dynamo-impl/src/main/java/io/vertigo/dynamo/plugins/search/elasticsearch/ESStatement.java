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
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListPatternFilterUtil;
import io.vertigo.dynamo.search.IndexFieldNameResolver;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.dynamo.search.model.Index;
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
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeBuilder;
import org.elasticsearch.search.aggregations.bucket.range.date.DateRangeBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.highlight.HighlightField;
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
	private final String indexName;
	private final Client esClient;
	private final ESDocumentCodec elasticSearchDocumentCodec;
	private final IndexFieldNameResolver indexFieldNameResolver;

	/**
	 * Constructeur.
	 * @param solrDocumentCodec Codec de traduction (bi-directionnelle) des objets métiers en document Solr
	 * @param esClient Client ElasticSearch.
	 * @param indexFieldNameResolver Resolver de nom de champ de l'index
	 */
	ESStatement(final ESDocumentCodec solrDocumentCodec, final String indexName, final Client esClient, final IndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkArgNotEmpty(indexName);
		Assertion.checkNotNull(solrDocumentCodec);
		Assertion.checkNotNull(esClient);
		Assertion.checkNotNull(indexFieldNameResolver);
		//---------------------------------------------------------------------
		this.indexName = indexName;
		this.esClient = esClient;
		this.elasticSearchDocumentCodec = solrDocumentCodec;
		this.indexFieldNameResolver = indexFieldNameResolver;
	}

	/**
	 * @param indexCollection Collection des indexes à insérer
	 */
	void putAll(final Collection<Index<I, R>> indexCollection) {
		//Injection spécifique au moteur d'indexation.
		try {
			final BulkRequestBuilder bulkRequest = esClient.prepareBulk();
			for (final Index<I, R> index : indexCollection) {
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
	void put(final Index<I, R> index) {
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
	void remove(final IndexDefinition indexDefinition, final ListFilter query) {
		Assertion.checkNotNull(query);
		//---------------------------------------------------------------------
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
	void remove(final IndexDefinition indexDefinition, final URI uri) {
		Assertion.checkNotNull(uri);
		//---------------------------------------------------------------------
		esClient.prepareDelete(indexName, uri.getDefinition().getName(), uri.toURN())
				.execute()
				.actionGet();
	}

	/**
	 * @param searchQuery Mots clés de recherche
	 * @param filtersQuery Filtrage par facette de la recherche
	 * @param rowsPerQuery Nombre de ligne max
	 * @return Résultat de la recherche
	 */
	FacetedQueryResult<R, SearchQuery> loadList(final SearchQuery searchQuery, final FacetedQuery filtersQuery, final int rowsPerQuery) {
		Assertion.checkNotNull(searchQuery);
		Assertion.checkNotNull(filtersQuery);
		//---------------------------------------------------------------------
		final SearchRequestBuilder searchRequestBuilder = createSearchRequestBuilder(searchQuery, filtersQuery, rowsPerQuery);

		appendFacetDefinition(filtersQuery.getDefinition(), searchRequestBuilder);

		//System.out.println("Query:" + solrQuery.toString());
		final SearchResponse queryResponse = searchRequestBuilder.execute().actionGet();
		return translateQuery(queryResponse, searchQuery, filtersQuery);
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

	private SearchRequestBuilder createSearchRequestBuilder(final SearchQuery searchQuery, final FacetedQuery filtersQuery, final int rowsPerQuery) {
		final SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(indexName)
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.addFields(ESDocumentCodec.FULL_RESULT)
				.setSize(rowsPerQuery);
		if (searchQuery.isSortActive()) {
			final DtField sortField = searchQuery.getIndexDefinition().getIndexDtDefinition().getField(searchQuery.getSortField());
			final String indexSortFieldName = indexFieldNameResolver.obtainIndexFieldName(sortField);
			searchRequestBuilder.addSort(indexSortFieldName, searchQuery.getSortAsc() ? SortOrder.ASC : SortOrder.DESC);
		}
		final QueryBuilder queryBuilder = translateToQueryBuilder(searchQuery.getListFilter(), indexFieldNameResolver);
		if (searchQuery.isBoostMostRecent()) {
			final QueryBuilder boostedQuery = appendBoostMostRecent(searchQuery, queryBuilder);
			searchRequestBuilder.setQuery(boostedQuery);
		} else {
			searchRequestBuilder.setQuery(queryBuilder);
		}

		final AndFilterBuilder filterBuilder = FilterBuilders.andFilter();
		for (final ListFilter facetQuery : filtersQuery.getListFilters()) {
			filterBuilder.add(translateToFilterBuilder(facetQuery, indexFieldNameResolver));
		}

		return searchRequestBuilder.setPostFilter(filterBuilder)
				.setHighlighterFilter(true)
				.setHighlighterNumOfFragments(3)
				.addHighlightedField("*");
	}

	private static QueryBuilder appendBoostMostRecent(final SearchQuery searchQuery, final QueryBuilder queryBuilder) {
		return QueryBuilders.functionScoreQuery(queryBuilder, new ExponentialDecayFunctionBuilder(searchQuery.getBoostedDocumentDateField(), null, searchQuery.getNumDaysOfBoostRefDocument() + "d").setDecay(searchQuery.getMostRecentBoost() - 1d));
	}

	private void appendFacetDefinition(final FacetedQueryDefinition queryDefinition, final SearchRequestBuilder searchRequestBuilder) {
		Assertion.checkNotNull(searchRequestBuilder);
		//---------------------------------------------------------------------
		for (final FacetDefinition facetDefinition : queryDefinition.getFacetDefinitions()) {
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
					searchRequestBuilder.addAggregation(dateRangeBuilder);
				} else {
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
					searchRequestBuilder.addAggregation(rangeBuilder);
				}
			} else {
				//facette par field
				final TermsBuilder aggregationBuilder = AggregationBuilders.terms(facetDefinition.getName())
						.field(indexFieldNameResolver.obtainIndexFieldName(dtField));
				searchRequestBuilder.addAggregation(aggregationBuilder);
			}
		}
	}

	private static final String DATE_PATTERN = "dd/MM/yy";
	private static final Pattern RANGE_PATTERN = Pattern.compile("([A-Z_0-9]+):([\\[\\]])(.*) TO (.*)([\\[\\]])");

	private static Option<Double> convertToDouble(final String valueToConvert) {
		final String stringValue = valueToConvert.trim();
		if ("*".equals(stringValue) || "".equals(stringValue)) {
			return Option.none();//pas de test
		}
		//--
		final Double result = Double.valueOf(stringValue);
		return Option.some(result);
	}

	/*private static String translateToQueryBuilder(final ListFilter query, final IndexFieldNameResolver indexFieldNameResolver) {
			Assertion.checkNotNull(query);
			//---------------------------------------------------------------------
			final StringBuilder stringQuery = new StringBuilder();
			//for (final QueryFilter facetQuery : queryFilters) {
			stringQuery.append(" +(");
			stringQuery.append(query.getFilterValue());
			stringQuery.append(')');
			//}
			return indexFieldNameResolver.replaceAllIndexFieldNames(stringQuery.toString());
		}*/
	private static QueryBuilder translateToQueryBuilder(final ListFilter listFilter, final IndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkNotNull(listFilter);
		//---------------------------------------------------------------------
		final String query = new StringBuilder()
				.append(" +(")
				.append(listFilter.getFilterValue())
				.append(')')
				.toString();
		return QueryBuilders.queryString(indexFieldNameResolver.replaceAllIndexFieldNames(query))
				.lowercaseExpandedTerms(false);
	}

	private static FilterBuilder translateToFilterBuilder(final ListFilter query, final IndexFieldNameResolver indexFieldNameResolver) {
		return FilterBuilders.queryFilter(translateToQueryBuilder(query, indexFieldNameResolver));
	}

	private FacetedQueryResult<R, SearchQuery> translateQuery(final SearchResponse queryResponse, final SearchQuery searchQuery, final FacetedQuery filtersQuery) {
		final IndexDefinition indexDefinition = searchQuery.getIndexDefinition();
		final Map<R, Map<DtField, String>> resultHighlights = new HashMap<>();
		final DtList<R> dtc = new DtList<>(indexDefinition.getResultDtDefinition());
		for (final SearchHit searchHit : queryResponse.getHits()) {
			final Index<I, R> index = elasticSearchDocumentCodec.searchHit2Index(indexDefinition, searchHit);
			final R result = index.getResultDtObject();
			dtc.add(result);

			final Map<DtField, String> highlights = createHighlight(searchHit, indexDefinition.getIndexDtDefinition(), indexFieldNameResolver);
			resultHighlights.put(result, highlights);
		}
		//On fabrique à la volée le résultat.
		final List<Facet> facets = createFacetList(filtersQuery.getDefinition(), queryResponse);
		final long count = queryResponse.getHits().getTotalHits();
		return new FacetedQueryResult<>(filtersQuery, count, dtc, facets, resultHighlights, searchQuery);
	}

	private static Map<DtField, String> createHighlight(final SearchHit searchHit, final DtDefinition indexDtDefinition, final IndexFieldNameResolver indexFieldNameResolver) {
		final Map<DtField, String> highlights = new HashMap<>();
		final Map<String, HighlightField> map = searchHit.getHighlightFields();
		for (final Map.Entry<String, HighlightField> entry : map.entrySet()) {
			final StringBuilder sb = new StringBuilder();
			for (final Text fragment : entry.getValue().getFragments()) {
				sb.append("<hlfrag>").append(fragment).append("</hlfrag>");
			}
			final DtField dtField = indexDtDefinition.getField(indexFieldNameResolver.obtainDtFieldName(entry.getKey()));
			highlights.put(dtField, sb.toString());
		}
		return highlights;
	}

	private static List<Facet> createFacetList(final FacetedQueryDefinition queryDefinition, final SearchResponse queryResponse) {
		final List<Facet> facets = new ArrayList<>();
		if (queryResponse.getAggregations() != null) {
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
			final String query = facetDefinition.getDtField().getName() + ":\"" + value.getKey() + "\"";
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
			final Range.Bucket value = rangeBuckets.getBucketByKey(facetRange.getListFilter().getFilterValue());
			rangeValues.put(facetRange, value.getDocCount());
		}
		return new Facet(facetDefinition, rangeValues);
	}
}
