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
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.impl.search.SearchServicesPlugin;
import io.vertigo.dynamo.search.SearchIndexFieldNameResolver;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.optimize.OptimizeRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;

/**
 * Gestion de la connexion au serveur Solr de manière transactionnel.
 * @author dchallas
 */
public abstract class AbstractESSearchServicesPlugin implements SearchServicesPlugin, Activeable {
	private static final SearchIndexFieldNameResolver DEFAULT_INDEX_FIELD_NAME_RESOLVER = new SearchIndexFieldNameResolver(Collections.<String, String> emptyMap());
	private static final Logger LOGGER = Logger.getLogger(AbstractESSearchServicesPlugin.class);
	private final ESDocumentCodec elasticDocumentCodec;

	private Node node;
	private Client esClient;
	private final Map<String, SearchIndexFieldNameResolver> indexFieldNameResolvers;
	private final int rowsPerQuery;
	private final Set<String> cores;

	/**
	 * Constructeur.
	 * @param cores Nom des noyeaux ES
	 * @param rowsPerQuery Nombre de lignes
	 * @param codecManager Manager de codec
	 */
	protected AbstractESSearchServicesPlugin(final String cores, final int rowsPerQuery, final CodecManager codecManager) {
		Assertion.checkArgNotEmpty(cores);
		Assertion.checkNotNull(codecManager);
		//-----
		this.rowsPerQuery = rowsPerQuery;
		elasticDocumentCodec = new ESDocumentCodec(codecManager);
		indexFieldNameResolvers = new HashMap<>();
		//------
		this.cores = new HashSet<>(Arrays.asList(cores.split(",")));

	}

	/** {@inheritDoc} */
	@Override
	public final void start() {
		//Init ElasticSearch Node
		node = createNode();
		node.start();
		esClient = node.client();
		for (final String core : cores) {
			final String indexName = core.toLowerCase().trim();
			waitForYellowStatus(); //must wait yellow status to be sure prepareExists works fine (instead of returning false on a already exist index)
			try {
				if (!esClient.admin().indices().prepareExists(indexName).execute().get().isExists()) {
					esClient.admin().indices().prepareCreate(indexName).execute().get();
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException("Error on index " + indexName, e);
			}
		}
		//Init typeMapping IndexDefinition <-> Conf ElasticSearch
		for (final SearchIndexDefinition indexDefinition : Home.getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			updateTypeMapping(indexDefinition);
			logMappings(indexDefinition);
		}

		waitForYellowStatus();
	}

	private void logMappings(final SearchIndexDefinition indexDefinition) {
		final IndicesAdminClient indicesAdmin = esClient.admin().indices();
		//logger.warn("Index " + indexName + " CurrentMapping:");
		final ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> indexMappings = indicesAdmin.prepareGetMappings(indexDefinition.getName().toLowerCase()).get().getMappings();
		for (final ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> indexMapping : indexMappings) {
			LOGGER.info("Index " + indexMapping.key + " CurrentMapping:");
			for (final ObjectObjectCursor<String, MappingMetaData> dtoMapping : indexMapping.value) {
				LOGGER.info(dtoMapping.key + " -> " + dtoMapping.value.source());
			}
		}
	}

	/**
	 * @return ElasticSearch node.
	 */
	protected abstract Node createNode();

	/** {@inheritDoc} */
	@Override
	public final void stop() {
		node.close();
	}

	/** {@inheritDoc} */
	@Override
	public final void registerIndexFieldNameResolver(final SearchIndexDefinition indexDefinition, final SearchIndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(indexFieldNameResolver);
		//-----
		indexFieldNameResolvers.put(indexDefinition.getName(), indexFieldNameResolver);
		updateTypeMapping(indexDefinition);
		logMappings(indexDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public final <I extends DtObject, R extends DtObject> void putAll(final SearchIndexDefinition indexDefinition, final Collection<SearchIndex<I, R>> indexCollection) {
		Assertion.checkNotNull(indexCollection);
		//-----
		final ESStatement<I, R> statement = createElasticStatement(indexDefinition);
		statement.putAll(indexCollection);
	}

	/** {@inheritDoc} */
	@Override
	public final <I extends DtObject, R extends DtObject> void put(final SearchIndexDefinition indexDefinition, final SearchIndex<I, R> index) {
		//On vérifie la cohérence des données SO et SOD.
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(index);
		Assertion.checkArgument(indexDefinition.equals(index.getDefinition()), "les Définitions ne sont pas conformes");
		//-----
		final ESStatement<I, R> statement = createElasticStatement(indexDefinition);
		statement.put(index);
	}

	/** {@inheritDoc} */
	@Override
	public final void remove(final SearchIndexDefinition indexDefinition, final URI uri) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(indexDefinition);
		//-----
		createElasticStatement(indexDefinition).remove(indexDefinition, uri);
		markToOptimize(indexDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public final <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery) {
		Assertion.checkNotNull(searchQuery);
		//-----
		final ESStatement<DtObject, R> statement = createElasticStatement(indexDefinition);
		return statement.loadList(indexDefinition, searchQuery, rowsPerQuery);
	}

	/** {@inheritDoc} */
	@Override
	public final long count(final SearchIndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//-----
		return createElasticStatement(indexDefinition).count();
	}

	/** {@inheritDoc} */
	@Override
	public final void remove(final SearchIndexDefinition indexDefinition, final ListFilter listFilter) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(listFilter);
		//-----
		createElasticStatement(indexDefinition).remove(indexDefinition, listFilter);
		markToOptimize(indexDefinition);
	}

	/**
	 * Fournit l' IndexFieldNameResolver d'un index.
	 * @param indexDefinition IndexDefinition de l'index
	 * @return IndexFieldNameResolver associé à l'index
	 */
	protected final SearchIndexFieldNameResolver obtainIndexFieldNameResolver(final SearchIndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//-----
		final SearchIndexFieldNameResolver indexFieldNameResolver = indexFieldNameResolvers.get(indexDefinition.getName());
		return indexFieldNameResolver != null ? indexFieldNameResolver : DEFAULT_INDEX_FIELD_NAME_RESOLVER;
	}

	private <I extends DtObject, R extends DtObject> ESStatement<I, R> createElasticStatement(final SearchIndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkArgument(cores.contains(indexDefinition.getName()), "Index {0} hasn't been registered (Registered indexes: {2}).", indexDefinition.getName(), cores);
		//-----
		return new ESStatement<>(elasticDocumentCodec, indexDefinition.getName().toLowerCase(), esClient, obtainIndexFieldNameResolver(indexDefinition));
	}

	/**
	 * Update template definition of this type.
	 * @param indexDefinition Index concerné
	 */
	private void updateTypeMapping(final SearchIndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//-----
		final SearchIndexFieldNameResolver indexFieldNameResolver = obtainIndexFieldNameResolver(indexDefinition);
		try (final XContentBuilder typeMapping = XContentFactory.jsonBuilder()) {
			typeMapping.startObject().startObject("properties")
					.startObject(ESDocumentCodec.FULL_RESULT)
					.field("type", "binary")
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
			final PutMappingResponse putMappingResponse = new PutMappingRequestBuilder(indicesAdmin)
					.setIndices(indexDefinition.getName().toLowerCase())
					.setType(indexDefinition.getIndexDtDefinition().getName())
					.setSource(typeMapping)
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
			case BigDecimal:
				if (fieldType == null) {
					throw new IllegalArgumentException("## Précisez la valeur \"indexType\" dans le domain [" + domain + "].");
				}
				break;
			case DataStream: // IllegalArgumentException
			case DtObject: // IllegalArgumentException
			case DtList: // IllegalArgumentException
			default: // IllegalArgumentException
				throw new IllegalArgumentException("Type de donnée non pris en charge pour l'indexation [" + domain + "].");

		}
		return fieldType;
	}

	private void markToOptimize(final SearchIndexDefinition indexDefinition) {
		esClient.admin().indices()
				.optimize(new OptimizeRequest(indexDefinition.getName().toLowerCase())
						.flush(true).maxNumSegments(32)); //32 files : empirique
	}

	private void waitForYellowStatus() {
		esClient.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
	}

}
