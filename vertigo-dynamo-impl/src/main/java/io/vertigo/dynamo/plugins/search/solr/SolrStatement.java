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
package io.vertigo.dynamo.plugins.search.solr;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.facet.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.facet.model.Facet;
import io.vertigo.dynamo.collections.facet.model.FacetValue;
import io.vertigo.dynamo.collections.facet.model.FacetedQuery;
import io.vertigo.dynamo.collections.facet.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.IndexFieldNameResolver;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.dynamo.search.model.Index;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;

//vérifier
/**
 * Requête physique d'accès à SOLR.
 * Le driver exécute les requêtes de façon synchrone dans le contexte transactionnelle de la ressource.
 * @author pchretien
 * @param <I> Type de l'objet contenant les champs à indexer
 * @param <R> Type de l'objet resultant de la recherche
 */
final class SolrStatement<I extends DtObject, R extends DtObject> {
	private final SolrServer solrServer;
	private final SolrDocumentCodec<I, R> solrDocumentCodec;
	private final IndexFieldNameResolver indexFieldNameResolver;

	/**
	 * Constructeur.
	 * @param solrDocumentCodec Codec de traduction (bi-directionnelle) des objets métiers en document Solr
	 * @param solrServer Serveur Solr.
	 * @param indexFieldNameResolver Resolver de nom de champ de l'index
	 */
	SolrStatement(final SolrDocumentCodec<I, R> solrDocumentCodec, final SolrServer solrServer, final IndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkNotNull(solrDocumentCodec);
		Assertion.checkNotNull(solrServer);
		Assertion.checkNotNull(indexFieldNameResolver);
		//---------------------------------------------------------------------
		this.solrServer = solrServer;
		this.solrDocumentCodec = solrDocumentCodec;
		this.indexFieldNameResolver = indexFieldNameResolver;
	}

	/**
	 * @param indexCollection Collection des indexes à insérer
	 */
	void putAll(final Collection<Index<I, R>> indexCollection) {
		//Injection spécifique au moteur d'indexation.
		try {
			final Collection<SolrInputDocument> solrInputDocuments = new ArrayList<>();
			for (final Index<I, R> index : indexCollection) {
				solrInputDocuments.add(solrDocumentCodec.index2SolrInputDocument(index, indexFieldNameResolver));
			}
			/*UpdateResponse updateResponse =*/solrServer.add(solrInputDocuments);
		} catch (final SolrServerException e) {
			throw new VRuntimeException(e);
		} catch (final IOException e) {
			handleIOException(e);
		}
	}

	private static void handleIOException(final IOException e) {
		throw new VRuntimeException("Serveur Solr indisponible", e);
	}

	/**
	 * @param index index à insérer
	 */
	void put(final Index<I, R> index) {
		//Injection spécifique au moteur d'indexation.
		try {
			final SolrInputDocument solrInputDocument = solrDocumentCodec.index2SolrInputDocument(index, indexFieldNameResolver);
			solrServer.add(solrInputDocument);
		} catch (final SolrServerException e) {
			throw new VRuntimeException(e);
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
		try {
			final String stringQuery = translateToSolr(query, indexFieldNameResolver);
			solrServer.deleteByQuery(stringQuery);
		} catch (final SolrServerException e) {
			throw new VRuntimeException(e);
		} catch (final IOException e) {
			handleIOException(e);
		}
	}

	/**
	 * Supprime un document.
	 * @param indexDefinition Index concerné
	 * @param uri Uri du document à supprimer
	 */
	void remove(final IndexDefinition indexDefinition, final URI uri) {
		Assertion.checkNotNull(uri);
		//---------------------------------------------------------------------
		try {
			solrServer.deleteById(uri.toURN());
		} catch (final SolrServerException e) {
			throw new VRuntimeException(e);
		} catch (final IOException e) {
			handleIOException(e);
		}
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
		final SolrQuery solrQuery = createSolrQuery(searchQuery, indexFieldNameResolver, filtersQuery, rowsPerQuery);
		appendFacetDefinition(filtersQuery.getDefinition(), solrQuery, indexFieldNameResolver);

		//System.out.println("Query:" + solrQuery.toString());
		final QueryResponse queryResponse = executeQuery(solrQuery);
		//		System.out.println("QueryResponse:" + queryResponse.toString());
		//		System.out.println("Highlight:");
		//		for (final String value : queryResponse.getHighlighting().keySet()) {
		//			System.out.println("    " + value + ": ");
		//			final Map<String, List<String>> map = queryResponse.getHighlighting().get(value);
		//			for (final String value2 : map.keySet()) {
		//				System.out.print("    " + "    " + value2 + ": ");
		//				final List<String> list = map.get(value2);
		//				for (final String value3 : list) {
		//					System.out.print(value3 + ", ");
		//				}
		//				System.out.print("\n");
		//			}
		//		}
		return translateQuery(searchQuery, filtersQuery, queryResponse);
	}

	/**
	 * @return Nombre de document indexés
	 */
	public long count() {
		final SolrQuery solrQuery = new SolrQuery();
		solrQuery.set(CommonParams.ROWS, 0);
		solrQuery.setQuery("*:*");
		final QueryResponse queryResponse = executeQuery(solrQuery);
		return queryResponse.getResults().getNumFound();
	}

	private static SolrQuery createSolrQuery(final SearchQuery searchQuery, final IndexFieldNameResolver indexFieldNameResolver, final FacetedQuery filtersQuery, final int rowsPerQuery) {
		final SolrQuery solrQuery = new SolrQuery();
		solrQuery.set(CommonParams.ROWS, rowsPerQuery);
		solrQuery.setFields(SolrDocumentCodec.URN, SolrDocumentCodec.FULL_RESULT);
		if (searchQuery.isSortActive()) {
			final DtField sortField = searchQuery.getIndexDefinition().getIndexDtDefinition().getField(searchQuery.getSortField());
			final String indexSortFieldName = indexFieldNameResolver.obtainIndexFieldName(sortField);
			solrQuery.addSortField(indexSortFieldName, searchQuery.getSortAsc() ? ORDER.asc : ORDER.desc);
		}
		//solrQuery.set(CommonParams.START, 0); //peut servir d'offset
		final StringBuilder query = new StringBuilder();
		if (searchQuery.isBoostMostRecent()) {
			appendBoostMostRecent(searchQuery, query);
		}
		query.append(translateToSolr(searchQuery.getListFilter(), indexFieldNameResolver));
		solrQuery.setQuery(query.toString());

		for (final ListFilter facetQuery : filtersQuery.getListFilters()) {
			solrQuery.addFilterQuery(translateToSolr(facetQuery, indexFieldNameResolver));
		}
		solrQuery.setHighlight(true);
		solrQuery.setParam("hl.fl", "*");
		solrQuery.setHighlightSnippets(3);
		solrQuery.setParam("hl.mergeContiguous", true);
		//Ci dessous : pour avoir les facettes avec un compteur de doc à 0
		//Pour l'instant désactivé car elles peuvent être déduites des définitions de facettes sauf pour celles tirées des mots du dictionnaires dont on ne maitrise pas la quantité
		//solrQuery.setParam("facet.missing", true);

		return solrQuery;
	}

	private static void appendBoostMostRecent(final SearchQuery searchQuery, final StringBuilder query) {
		//formule f(x) = recip(x,m,a,b) = a/(mx+b) (x sera l'age du doc arondit au jour en millis )
		final double m = 1 / (searchQuery.getNumDaysOfBoostRefDocument() * 24 * 60 * 60 * 1000d);
		final double a = 1;
		final double b = 1 / (searchQuery.getMostRecentBoost() - 1d);
		query.append("{!boost b=recip(ms(NOW/DAY,").append(searchQuery.getBoostedDocumentDateField()).append("),");
		query.append(m).append(",").append(a).append(",").append(b);
		query.append(")}");
	}

	private static void appendFacetDefinition(final FacetedQueryDefinition queryDefinition, final SolrQuery solrQuery, final IndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkNotNull(solrQuery);
		//---------------------------------------------------------------------
		//Activation des facettes 
		final boolean hasFacet = !queryDefinition.getFacetDefinitions().isEmpty();
		solrQuery.setFacet(hasFacet);

		for (final FacetDefinition facetDefinition : queryDefinition.getFacetDefinitions()) {
			//Récupération des noms des champs correspondant aux facettes.
			final DtField dtField = facetDefinition.getDtField();
			if (facetDefinition.isRangeFacet()) {
				//facette par range 
				for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
					solrQuery.addFacetQuery(translateToSolr(facetRange.getListFilter(), indexFieldNameResolver));
				}
			} else {
				//facette par field 
				solrQuery.addFacetField(indexFieldNameResolver.obtainIndexFieldName(dtField));
			}
		}
	}

	private static String translateToSolr(final ListFilter query, final IndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkNotNull(query);
		//---------------------------------------------------------------------
		final StringBuilder stringQuery = new StringBuilder();
		//for (final QueryFilter facetQuery : queryFilters) {
		stringQuery.append(" +(");
		stringQuery.append(query.getFilterValue());
		stringQuery.append(')');
		//}
		return indexFieldNameResolver.replaceAllIndexFieldNames(stringQuery.toString());
	}

	private QueryResponse executeQuery(final SolrQuery solrQuery) {
		final QueryResponse queryResponse;
		try {
			queryResponse = solrServer.query(solrQuery);
		} catch (final SolrServerException e) {
			throw new VRuntimeException(e);
		}
		return queryResponse;
	}

	private FacetedQueryResult<R, SearchQuery> translateQuery(final SearchQuery searchQuery, final FacetedQuery filtersQuery, final QueryResponse queryResponse) {
		final IndexDefinition indexDefinition = searchQuery.getIndexDefinition();
		final Map<R, Map<DtField, String>> resultHighlights = new HashMap<>();
		final DtList<R> dtc = new DtList<>(indexDefinition.getResultDtDefinition());
		for (final SolrDocument solrDocument : queryResponse.getResults()) {
			final Index<I, R> index = solrDocumentCodec.solrDocument2Index(indexDefinition, solrDocument);
			final R result = index.getResultDtObject();
			dtc.add(result);

			final Map<DtField, String> highlights = createHighlight(queryResponse, solrDocument, indexDefinition.getIndexDtDefinition());
			resultHighlights.put(result, highlights);
		}
		//On fabrique à la volée le résultat.
		final List<Facet> facetList = createFacetList(filtersQuery.getDefinition(), queryResponse);
		final long count = queryResponse.getResults().getNumFound();

		return new FacetedQueryResult<>(filtersQuery, count, dtc, facetList, resultHighlights, searchQuery);
	}

	private static Map<DtField, String> createHighlight(final QueryResponse queryResponse, final SolrDocument solrDocument, final DtDefinition indexDtDefinition) {
		final Map<DtField, String> highlights = new HashMap<>();
		final Map<String, List<String>> map = queryResponse.getHighlighting().get(solrDocument.get("URI"));
		for (final Map.Entry<String, List<String>> entry : map.entrySet()) {
			if (!"URI".equals(entry.getKey())) { //solr retourne un highlight sur l'uri pour d'obscures raisons..
				final StringBuilder sb = new StringBuilder();
				for (final String fragment : entry.getValue()) {
					sb.append("<hlfrag>").append(fragment).append("</hlfrag>");
				}
				final DtField dtField = indexDtDefinition.getField(entry.getKey());//TODO : unmap dynfield
				highlights.put(dtField, sb.toString());
			}
		}
		return highlights;
	}

	private List<Facet> createFacetList(final FacetedQueryDefinition queryDefinition, final QueryResponse queryResponse) {
		final List<Facet> facetList = new ArrayList<>();
		//Pour chaque type de facette
		for (final FacetDefinition facetDefinition : queryDefinition.getFacetDefinitions()) {
			if (facetDefinition.isRangeFacet()) {
				//Cas des facettes par 'range' 
				final Map<String, Integer> responseFacetQuery = queryResponse.getFacetQuery();
				final Facet currentFacet = createFacetRange(facetDefinition, responseFacetQuery);
				facetList.add(currentFacet);
			} else {
				//Cas des facettes par 'term'
				final FacetField facetField = queryResponse.getFacetField(indexFieldNameResolver.obtainIndexFieldName(facetDefinition.getDtField()));
				//On vérifie que si SOLR a trouvé des valeurs (SOLR utilise null quand aucune valeur n'est trouvée)
				//facetField peut être null !! (merci Solr)
				if (facetField != null && facetField.getValues() != null) {
					final Facet currentFacet = createTermFacet(facetDefinition, facetField);
					facetList.add(currentFacet);
				}
			}
		}
		return facetList;
	}

	private static Facet createTermFacet(final FacetDefinition facetDefinition, final FacetField facetField) {
		final Map<FacetValue, Long> facetValues = new HashMap<>();
		FacetValue facetValue;
		for (final Count values : facetField.getValues()) {
			final MessageText label = new MessageText(values.getName(), null);
			final String query = facetDefinition.getDtField().getName() + ":\"" + values.getName() + "\"";
			facetValue = new FacetValue(new ListFilter(query), label);
			facetValues.put(facetValue, values.getCount());
		}

		//tri des facettes
		final Comparator<FacetValue> facetComparator = new Comparator<FacetValue>() {
			public int compare(final FacetValue o1, final FacetValue o2) {
				final int compareNbDoc = (int) (facetValues.get(o2) - facetValues.get(o1));
				return compareNbDoc != 0 ? compareNbDoc : o1.getLabel().getDisplay().compareToIgnoreCase(o2.getLabel().getDisplay());
			}
		};
		final Map<FacetValue, Long> sortedFacetValues = new TreeMap<>(facetComparator);
		sortedFacetValues.putAll(facetValues);

		return new Facet(facetDefinition, sortedFacetValues);
	}

	private Facet createFacetRange(final FacetDefinition facetDefinition, final Map<String, Integer> responseFacetQuery) {
		//Cas des facettes par range 
		final Map<FacetValue, Long> rangeValues = new LinkedHashMap<>();
		for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
			final String rangeQueryString = translateToSolr(facetRange.getListFilter(), indexFieldNameResolver);
			final long count = responseFacetQuery.get(rangeQueryString) == null ? 0 : responseFacetQuery.get(rangeQueryString);
			rangeValues.put(facetRange, count);
		}
		return new Facet(facetDefinition, rangeValues);
	}
}
