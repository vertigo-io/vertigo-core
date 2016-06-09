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
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.impl.search.SearchRessources;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.VUserException;
import io.vertigo.lang.WrappedException;

import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;

//vérifier
/**
 * Requête physique d'accès à ElasticSearch.
 * Le driver exécute les requêtes de façon synchrone dans le contexte transactionnelle de la ressource.
 * @author pchretien, npiedeloup
 * @param <I> Type de l'objet représentant l'index
 * @param <K> Type du keyConcept métier indexé
 */
final class ESStatement<K extends KeyConcept, I extends DtObject> {

	static final String TOPHITS_SUBAGGREAGTION_NAME = "top";
	private static final Logger LOGGER = Logger.getLogger(ESStatement.class);

	private final String indexName;
	private final String typeName;
	private final Client esClient;
	private final ESDocumentCodec esDocumentCodec;

	/**
	 * Constructeur.
	 * @param esDocumentCodec Codec de traduction (bi-directionnelle) des objets métiers en document
	 * @param indexName Index name
	 * @param typeName Type name in Index
	 * @param esClient Client ElasticSearch.
	 */
	ESStatement(final ESDocumentCodec esDocumentCodec, final String indexName, final String typeName, final Client esClient) {
		Assertion.checkArgNotEmpty(indexName);
		Assertion.checkArgNotEmpty(typeName);
		Assertion.checkNotNull(esDocumentCodec);
		Assertion.checkNotNull(esClient);
		//-----
		this.indexName = indexName;
		this.typeName = typeName;
		this.esClient = esClient;
		this.esDocumentCodec = esDocumentCodec;
	}

	/**
	 * @param indexCollection Collection des indexes à insérer
	 */
	void putAll(final Collection<SearchIndex<K, I>> indexCollection) {
		//Injection spécifique au moteur d'indexation.
		try {
			final BulkRequestBuilder bulkRequest = esClient.prepareBulk();
			for (final SearchIndex<K, I> index : indexCollection) {
				try (final XContentBuilder xContentBuilder = esDocumentCodec.index2XContentBuilder(index)) {
					bulkRequest.add(esClient.prepareIndex()
							.setIndex(indexName)
							.setType(typeName)
							.setId(index.getURI().urn())
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
		throw new WrappedException("Serveur ElasticSearch indisponible", e);
	}

	/**
	 * @param index index à insérer
	 */
	void put(final SearchIndex<K, I> index) {
		//Injection spécifique au moteur d'indexation.
		try (final XContentBuilder xContentBuilder = esDocumentCodec.index2XContentBuilder(index)) {
			esClient.prepareIndex()
					.setIndex(indexName)
					.setType(typeName)
					.setId(index.getURI().urn())
					.setSource(xContentBuilder)
					.execute() //execute asynchrone
					.actionGet(); //get wait exec
		} catch (final IOException e) {
			handleIOException(e);
		}
	}

	/**
	 * Supprime des documents.
	 * @param query Requete de filtrage des documents à supprimer
	 */
	void remove(final ListFilter query) {
		Assertion.checkNotNull(query);
		//-----
		final QueryBuilder queryBuilder = ESSearchRequestBuilder.translateToQueryBuilder(query);
		esClient.prepareDeleteByQuery(indexName)
				.setTypes(typeName)
				.setQuery(queryBuilder)
				.execute()
				.actionGet();
	}

	/**
	 * Supprime un document.
	 * @param uri Uri du document à supprimer
	 */
	void remove(final URI uri) {
		Assertion.checkNotNull(uri);
		//-----
		esClient.prepareDelete()
				.setIndex(indexName)
				.setType(typeName)
				.setId(uri.urn())
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
	FacetedQueryResult<I, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState, final int defaultMaxRows) {
		Assertion.checkNotNull(searchQuery);
		//-----
		final SearchRequestBuilder searchRequestBuilder = new ESSearchRequestBuilder(indexName, typeName, esClient)
				.withSearchIndexDefinition(indexDefinition)
				.withSearchQuery(searchQuery)
				.withListState(listState, defaultMaxRows)
				.build();
		LOGGER.info("loadList " + searchRequestBuilder);
		try {
			final SearchResponse queryResponse = searchRequestBuilder.execute().actionGet();
			return new ESFacetedQueryResultBuilder(esDocumentCodec, indexDefinition, queryResponse, searchQuery)
					.build();
		} catch (final SearchPhaseExecutionException e) {
			final VUserException vue = new VUserException(
					new MessageText(SearchRessources.DYNAMO_SEARCH_QUERY_SYNTAX_ERROR));
			vue.initCause(e);
			throw vue;
		}
	}

	/**
	 * @return Nombre de document indexés
	 */
	public long count() {
		final CountResponse response = esClient.prepareCount()
				.setIndices(indexName)
				.setTypes(typeName)
				.execute()
				.actionGet();
		return response.getCount();
	}
}
