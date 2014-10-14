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

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.Home;
import io.vertigo.core.lang.Activeable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.IndexFieldNameResolver;
import io.vertigo.dynamo.search.SearchServicesPlugin;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.dynamo.search.model.Index;
import io.vertigo.dynamo.search.model.SearchQuery;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.optimize.OptimizeRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

/**
 * Gestion de la connexion au serveur Solr de manière transactionnel.
 * @author dchallas
 */
public abstract class AbstractElasticSearchServicesPlugin implements SearchServicesPlugin, Activeable {
	private static final IndexFieldNameResolver DEFAULT_INDEX_FIELD_NAME_RESOLVER = new IndexFieldNameResolver(Collections.<String, String> emptyMap());

	private final ElasticDocumentCodec elasticDocumentCodec;

	private Client esClient;
	private final Map<String, IndexFieldNameResolver> indexFieldNameResolvers;
	private final int rowsPerQuery;
	private final Set<String> cores;
	//private final String[] indices;
	private boolean typeMappingInitialized = false;

	/**
	 * Constructeur.
	 * @param cores Nom des noyeaux ES
	 * @param rowsPerQuery Nombre de lignes
	 * @param codecManager Manager de codec
	 */
	protected AbstractElasticSearchServicesPlugin(final String cores, final int rowsPerQuery, final CodecManager codecManager) {
		Assertion.checkArgNotEmpty(cores);
		Assertion.checkNotNull(codecManager);
		//---------------------------------------------------------------------
		this.rowsPerQuery = rowsPerQuery;
		elasticDocumentCodec = new ElasticDocumentCodec(codecManager);
		indexFieldNameResolvers = new HashMap<>();
		//------
		//indices = cores.toLowerCase().split(",");
		this.cores = new HashSet<>(Arrays.asList(cores.split(",")));

	}

	/** {@inheritDoc} */
	public final void start() {

		esClient = createEsClient();
		initIndicesSettings();
		for (final String core : cores) {
			final String indexName = core.toLowerCase();
			if (!esClient.admin().indices().prepareExists(indexName).execute().actionGet().isExists()) {
				esClient.admin().indices().prepareCreate(indexName).execute().actionGet();
			}
		}
	}

	/*private static final String DEFAULT_SETTINGS = //
	"{\"index\" : {\"analysis\" : {" //
			+ "	\"analyzer\" : {" //
			+ "		\"default\" : {" //
			+ "			\"tokenizer\" : \"standard\"," //
			+ "			\"filter\" : [\"standard\", \"elision\"]" //
			+ "		}" //
			+ "	}," //
			+ "	\"filter\" : {" //
			+ "		\"elision\" : {" //
			+ "			\"type\" : \"elision\"," //
			+ "			\"articles\" : [\"l\", \"m\", \"t\", \"qu\", \"n\", \"s\", \"j\"]" //
			+ "		}" //
			+ "	}" //
			+ "}}}";*/

	private void initIndicesSettings() {
		//esClient.admin().indices().prepareUpdateSettings()//
		//.setSettings(DEFAULT_SETTINGS) //
		//.execute().actionGet();
	}

	/** {@inheritDoc} */
	public final void registerIndexFieldNameResolver(final IndexDefinition indexDefinition, final IndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(indexFieldNameResolver);
		//---------------------------------------------------------------------
		indexFieldNameResolvers.put(indexDefinition.getName(), indexFieldNameResolver);
		typeMappingInitialized = false;
	}

	/** {@inheritDoc} */
	public final <I extends DtObject, R extends DtObject> void putAll(final IndexDefinition indexDefinition, final Collection<Index<I, R>> indexCollection) {
		Assertion.checkNotNull(indexCollection);
		//---------------------------------------------------------------------
		final ElasticStatement<I, R> statement = createElasticStatement(indexDefinition);
		statement.putAll(indexCollection);
	}

	/** {@inheritDoc} */
	public final <I extends DtObject, R extends DtObject> void put(final IndexDefinition indexDefinition, final Index<I, R> index) {
		//On vérifie la cohérence des données SO et SOD.
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(index);
		Assertion.checkArgument(indexDefinition.equals(index.getDefinition()), "les Définitions ne sont pas conformes");
		//---------------------------------------------------------------------
		final ElasticStatement<I, R> statement = createElasticStatement(indexDefinition);
		statement.put(index);
	}

	/** {@inheritDoc} */
	public final void remove(final IndexDefinition indexDefinition, final URI uri) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(indexDefinition);
		//---------------------------------------------------------------------
		createElasticStatement(indexDefinition).remove(indexDefinition, uri);
		markToOptimize(indexDefinition);
	}

	/** {@inheritDoc} */
	public final <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchQuery searchQuery, final FacetedQuery facetedQuery) {
		Assertion.checkNotNull(searchQuery);
		Assertion.checkNotNull(facetedQuery);
		//---------------------------------------------------------------------
		final ElasticStatement<DtObject, R> statement = createElasticStatement(searchQuery.getIndexDefinition());
		return statement.loadList(searchQuery, facetedQuery, rowsPerQuery);
	}

	/** {@inheritDoc} */
	public final long count(final IndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//---------------------------------------------------------------------
		return createElasticStatement(indexDefinition).count();
	}

	/** {@inheritDoc} */
	public final void remove(final IndexDefinition indexDefinition, final ListFilter listFilter) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(listFilter);
		//---------------------------------------------------------------------
		createElasticStatement(indexDefinition).remove(indexDefinition, listFilter);
		markToOptimize(indexDefinition);
	}

	/**
	 * @return ElastcSearch Client
	 */
	protected abstract Client createEsClient();

	/**
	 * Fournit l' IndexFieldNameResolver d'un index.
	 * @param indexDefinition IndexDefinition de l'index
	 * @return IndexFieldNameResolver associé à l'index
	 */
	protected final IndexFieldNameResolver obtainIndexFieldNameResolver(final IndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//---------------------------------------------------------------------
		final IndexFieldNameResolver indexFieldNameResolver = indexFieldNameResolvers.get(indexDefinition.getName());
		return indexFieldNameResolver != null ? indexFieldNameResolver : DEFAULT_INDEX_FIELD_NAME_RESOLVER;
	}

	private <I extends DtObject, R extends DtObject> ElasticStatement<I, R> createElasticStatement(final IndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkArgument(cores.contains(indexDefinition.getName()), "Index {0} hasn't been registered (Registered indexes: {2}).", indexDefinition.getName(), cores);
		checkInitialized();
		//---------------------------------------------------------------------
		return new ElasticStatement<>(elasticDocumentCodec, indexDefinition.getName().toLowerCase(), esClient, obtainIndexFieldNameResolver(indexDefinition));
	}

	private void checkInitialized() {
		if (!typeMappingInitialized) {
			synchronized (this) { //double check locking OK for primitive like boolean
				if (!typeMappingInitialized) {
					for (final IndexDefinition indexDefinition : Home.getDefinitionSpace().getAll(IndexDefinition.class)) {
						updateTypeMapping(indexDefinition);
					}
					typeMappingInitialized = true;
				}
			}
		}
	}

	/**
	 * Update template definition of this type.
	 * @param indexDefinition Index concerné
	 */
	private void updateTypeMapping(final IndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//---------------------------------------------------------------------
		final IndexFieldNameResolver indexFieldNameResolver = obtainIndexFieldNameResolver(indexDefinition);
		try (final XContentBuilder typeMapping = XContentFactory.jsonBuilder()) {
			typeMapping.startObject().startObject("properties") //
					.startObject(ElasticDocumentCodec.FULL_RESULT) //
					.field("type", "binary")//
					.endObject();
			/* 3 : Les champs du dto index */
			final DtDefinition indexDtDefinition = indexDefinition.getIndexDtDefinition();
			for (final DtField dtField : indexDtDefinition.getFields()) {
				final String indexType = resolveIndexType(dtField.getDomain());
				//final String fieldType = dtField.getDomain().getProperties().getValue(DtProperty.INDEX_TYPE);
				//final String indexType = "{" + resolveIndexType(dtField.getDomain()) + "}";
				//typeMapping.rawField(dtField.getName(), indexType.getBytes("utf8"));
				if (indexType != null) {
					typeMapping.startObject(indexFieldNameResolver.obtainIndexFieldName(dtField));
					typeMapping.field("type", "string").field("analyzer", indexType); //par convention l'indextype du domain => l'analyzer de l'index
					typeMapping.endObject();
				}
			}
			typeMapping.endObject().endObject(); //end properties
			//
			final IndicesAdminClient indicesAdmin = esClient.admin().indices();
			final PutMappingResponse putMappingResponse = new PutMappingRequestBuilder(indicesAdmin) //
					.setIndices(indexDefinition.getName().toLowerCase()) //
					.setType(indexDefinition.getIndexDtDefinition().getName()) //
					.setSource(typeMapping)//
					.get();
			putMappingResponse.isAcknowledged();
		} catch (final IOException e) {
			throw new RuntimeException("Serveur ElasticSearch indisponible", e);
		}
	}

	private String resolveIndexType(final Domain domain) {
		// On peut préciser pour chaque domaine le type d'indexation
		final String fieldType = domain.getProperties().getValue(DtProperty.INDEX_TYPE);
		// Calcul automatique  par default.
		switch (domain.getDataType()) {
			case Boolean: // native
			case Date: // native
			case Double: // native
			case Integer: // native
			case Long: // native
				break;
			case String:
				if (fieldType == null) {
					throw new IllegalArgumentException("## Précisez la valeur \"indexType\" dans le domain [" + domain + "].");
				}
				break;
			case DataStream: // IllegalArgumentException
			case BigDecimal: // IllegalArgumentException
			case DtObject: // IllegalArgumentException
			case DtList: // IllegalArgumentException
			default: // IllegalArgumentException
				throw new IllegalArgumentException("Type de donnée non pris en charge pour l'indexation [" + domain + "].");

		}
		return fieldType;
	}

	private void markToOptimize(final IndexDefinition indexDefinition) {
		esClient.admin().indices().optimize(new OptimizeRequest().flush(true).maxNumSegments(32)); //32 files : empirique
	}

}
