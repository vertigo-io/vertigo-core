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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;

import io.vertigo.app.Home;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.impl.search.SearchServicesPlugin;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Gestion de la connexion au serveur Solr de manière transactionnel.
 * @author dchallas
 */
public abstract class AbstractESSearchServicesPlugin implements SearchServicesPlugin, Activeable {
	private static final int OPTIMIZE_MAX_NUM_SEGMENT = 32;
	private static final Logger LOGGER = Logger.getLogger(AbstractESSearchServicesPlugin.class);
	private final ESDocumentCodec elasticDocumentCodec;

	private Client esClient;
	private final DtListState defaultListState;
	private final int defaultMaxRows;
	private final String indexName;
	private final Set<String> types = new HashSet<>();
	private final URL configFile;
	private boolean indexSettingsValid = false;

	/**
	 * Constructeur.
	 * @param indexName Nom de l'index ES
	 * @param defaultMaxRows Nombre de lignes
	 * @param codecManager Manager de codec
	 * @param configFile Fichier de configuration des indexs
	 * @param resourceManager Manager des resources
	 */
	protected AbstractESSearchServicesPlugin(final String indexName, final int defaultMaxRows, final Optional<String> configFile,
			final CodecManager codecManager, final ResourceManager resourceManager) {
		Assertion.checkArgNotEmpty(indexName);
		Assertion.checkNotNull(codecManager);
		//-----
		this.defaultMaxRows = defaultMaxRows;
		defaultListState = new DtListState(defaultMaxRows, 0, null, null);
		elasticDocumentCodec = new ESDocumentCodec(codecManager);
		//------
		this.indexName = indexName.toLowerCase(Locale.ENGLISH).trim();
		if (configFile.isPresent()) {
			this.configFile = resourceManager.resolve(configFile.get());
		} else {
			this.configFile = null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public final void start() {
		//Init ElasticSearch Client
		esClient = createClient();
		indexSettingsValid = true;
		//must wait yellow status to be sure prepareExists works fine (instead of returning false on a already exist index)
		waitForYellowStatus();
		try {
			if (!esClient.admin().indices().prepareExists(indexName).get().isExists()) {
				if (configFile == null) {
					esClient.admin().indices().prepareCreate(indexName).get();
				} else {
					try (InputStream is = configFile.openStream()) {
						final Settings settings = Settings.settingsBuilder().loadFromStream(configFile.getFile(), is).build();
						esClient.admin().indices().prepareCreate(indexName).setSettings(settings).get();
					}
				}
			} else if (configFile != null) {
				// If we use local config file, we check config against ES server
				try (InputStream is = configFile.openStream()) {
					final Settings settings = Settings.settingsBuilder().loadFromStream(configFile.getFile(), is).build();
					indexSettingsValid = indexSettingsValid && !isIndexSettingsDirty(settings);
				}
			}
		} catch (final ElasticsearchException | IOException e) {
			throw new WrappedException("Error on index " + indexName, e);
		}
		//Init typeMapping IndexDefinition <-> Conf ElasticSearch
		for (final SearchIndexDefinition indexDefinition : Home.getApp().getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			updateTypeMapping(indexDefinition);
			logMappings();
			types.add(indexDefinition.getName().toLowerCase(Locale.ENGLISH));
		}

		waitForYellowStatus();
	}

	private boolean isIndexSettingsDirty(final Settings settings) {
		final Settings currentSettings = esClient.admin()
				.indices()
				.prepareGetIndex()
				.addIndices(indexName)
				.get()
				.getSettings()
				.get(indexName);
		boolean indexSettingsDirty = false;
		final Map<String, String> settingsMap = settings.getAsMap();
		for (final Entry<String, String> entry : settingsMap.entrySet()) {
			final String currentValue = currentSettings.get(entry.getKey());
			if (currentValue == null) {
				indexSettingsDirty = true;
				break;
			}
			final String expectedValue = entry.getValue();
			if (!currentValue.equals(expectedValue)) {
				indexSettingsDirty = true;
				LOGGER.warn("[" + indexName + "] " + entry.getKey() + ":  current=" + currentValue + ", expected=" + expectedValue);
				break;
			}
		}
		return indexSettingsDirty;
	}

	private void logMappings() {
		final IndicesAdminClient indicesAdmin = esClient.admin().indices();
		final ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> indexMappings = indicesAdmin.prepareGetMappings(indexName).get().getMappings();
		for (final ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> indexMapping : indexMappings) {
			LOGGER.info("Index " + indexMapping.key + " CurrentMapping:");
			for (final ObjectObjectCursor<String, MappingMetaData> dtoMapping : indexMapping.value) {
				LOGGER.info(dtoMapping.key + " -> " + dtoMapping.value.source());
			}
		}
	}

	/**
	 * @return ElasticSearch client.
	 */
	protected abstract Client createClient();

	/**
	 * Close created client.
	 */
	protected abstract void closeClient();

	/** {@inheritDoc} */
	@Override
	public final void stop() {
		closeClient();
	}

	/** {@inheritDoc} */
	@Override
	public final <S extends KeyConcept, I extends DtObject> void putAll(final SearchIndexDefinition indexDefinition, final Collection<SearchIndex<S, I>> indexCollection) {
		Assertion.checkNotNull(indexCollection);
		//-----
		final ESStatement<S, I> statement = createElasticStatement(indexDefinition);
		statement.putAll(indexCollection);
	}

	/** {@inheritDoc} */
	@Override
	public final <S extends KeyConcept, I extends DtObject> void put(final SearchIndexDefinition indexDefinition, final SearchIndex<S, I> index) {
		//On vérifie la cohérence des données SO et SOD.
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(index);
		Assertion.checkArgument(indexDefinition.equals(index.getDefinition()), "les Définitions ne sont pas conformes");
		//-----
		final ESStatement<S, I> statement = createElasticStatement(indexDefinition);
		statement.put(index);
	}

	/** {@inheritDoc} */
	@Override
	public final <S extends KeyConcept> void remove(final SearchIndexDefinition indexDefinition, final URI<S> uri) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(indexDefinition);
		//-----
		createElasticStatement(indexDefinition).remove(uri);
		markToOptimize();
	}

	/** {@inheritDoc} */
	@Override
	public final <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState) {
		Assertion.checkNotNull(searchQuery);
		//-----
		final ESStatement<KeyConcept, R> statement = createElasticStatement(indexDefinition);
		final DtListState usedListState = listState != null ? listState : defaultListState;
		return statement.loadList(indexDefinition, searchQuery, usedListState, defaultMaxRows);
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
		createElasticStatement(indexDefinition).remove(listFilter);
		markToOptimize();
	}

	private <S extends KeyConcept, I extends DtObject> ESStatement<S, I> createElasticStatement(final SearchIndexDefinition indexDefinition) {
		Assertion.checkArgument(indexSettingsValid, "Index settings have changed and are no more compatible, you must recreate your index : stop server, delete your index data folder, restart server and launch indexation job.");
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkArgument(types.contains(indexDefinition.getName().toLowerCase(Locale.ENGLISH)), "Type {0} hasn't been registered (Registered type: {1}).", indexDefinition.getName(), types);
		//-----
		return new ESStatement<>(elasticDocumentCodec, indexName, indexDefinition.getName().toLowerCase(Locale.ENGLISH), esClient);
	}

	/**
	 * Update template definition of this type.
	 * @param indexDefinition Index concerné
	 */
	private void updateTypeMapping(final SearchIndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//-----
		try (final XContentBuilder typeMapping = XContentFactory.jsonBuilder()) {
			typeMapping.startObject()
					.startObject("properties")
					.startObject(ESDocumentCodec.FULL_RESULT)
					.field("type", "binary")
					.endObject();
			/* 3 : Les champs du dto index */
			final Set<DtField> copyFromFields = indexDefinition.getIndexCopyFromFields();
			final DtDefinition indexDtDefinition = indexDefinition.getIndexDtDefinition();
			for (final DtField dtField : indexDtDefinition.getFields()) {
				final Optional<IndexType> indexType = IndexType.readIndexType(dtField.getDomain());
				if (indexType.isPresent() || copyFromFields.contains(dtField)) {
					typeMapping.startObject(dtField.getName());
					if (indexType.isPresent()) {
						appendIndexTypeMapping(typeMapping, indexType.get());
					} else {
						//sinon, il faut le type par défaut
						typeMapping.field("type", IndexType.obtainDefaultIndexDataType(dtField.getDomain()));
					}
					if (copyFromFields.contains(dtField)) {
						appendIndexCopyToMapping(indexDefinition, typeMapping, dtField);
					}
					typeMapping.endObject();
				}
			}
			typeMapping.endObject().endObject(); //end properties

			final PutMappingResponse putMappingResponse = esClient.admin()
					.indices()
					.preparePutMapping(indexName)
					.setType(indexDefinition.getName().toLowerCase(Locale.ENGLISH))
					.setSource(typeMapping)
					.get();
			putMappingResponse.isAcknowledged();

		} catch (final IOException e) {
			throw new WrappedException("Serveur ElasticSearch indisponible", e);
		}
	}

	private static void appendIndexCopyToMapping(final SearchIndexDefinition indexDefinition, final XContentBuilder typeMapping, final DtField dtField) throws IOException {
		final List<DtField> copyToFields = indexDefinition.getIndexCopyToFields(dtField);
		if (copyToFields.size() == 1) {
			typeMapping.field("copy_to", copyToFields.get(0).getName());
		} else {
			final String[] copyToFieldNames = new String[copyToFields.size()];
			for (int i = 0; i < copyToFieldNames.length; i++) {
				copyToFieldNames[i] = copyToFields.get(i).getName();
			}
			typeMapping.field("copy_to", copyToFieldNames);
		}
	}

	private static void appendIndexTypeMapping(final XContentBuilder typeMapping, final IndexType indexType) throws IOException {
		typeMapping.field("type", indexType.getIndexDataType());
		typeMapping.field("analyzer", indexType.getIndexAnalyzer());
	}

	private void markToOptimize() {
		esClient.admin()
				.indices()
				.prepareForceMerge(indexName)
				.setFlush(true)
				.setMaxNumSegments(OPTIMIZE_MAX_NUM_SEGMENT)//32 files : empirique
				.execute()
				.actionGet();
	}

	private void waitForYellowStatus() {
		esClient.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
	}

}
