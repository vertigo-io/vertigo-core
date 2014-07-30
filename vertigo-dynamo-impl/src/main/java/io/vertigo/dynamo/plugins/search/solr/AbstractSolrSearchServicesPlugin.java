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

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.IndexFieldNameResolver;
import io.vertigo.dynamo.search.SearchServicesPlugin;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.dynamo.search.model.Index;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.kernel.lang.Assertion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;

/**
 * Gestion de la connexion au serveur Solr de manière transactionnel.
 * @author dchallas
 */
public abstract class AbstractSolrSearchServicesPlugin implements SearchServicesPlugin {
	private static final IndexFieldNameResolver DEFAULT_INDEX_FIELD_NAME_RESOLVER = new IndexFieldNameResolver(Collections.<String, String> emptyMap());

	private final CodecManager codecManager;
	private final Map<String, SolrServer> solrServerMap;
	private final Map<String, IndexFieldNameResolver> indexFieldNameResolverMap;
	private final int rowsPerQuery;

	/**
	 * Constructeur.
	 * @param cores Nom des noyeau Solr
	 * @param rowsPerQuery Nombre de ligne
	 * @param codecManager Manager de codec
	 */
	protected AbstractSolrSearchServicesPlugin(final String[] cores, final int rowsPerQuery, final CodecManager codecManager) {
		Assertion.checkNotNull(cores);
		Assertion.checkNotNull(codecManager);
		//---------------------------------------------------------------------
		this.rowsPerQuery = rowsPerQuery;
		this.codecManager = codecManager;
		solrServerMap = new HashMap<>();
		indexFieldNameResolverMap = new HashMap<>();
		//------
		//SOLR appelle constitue un server par indexe/Core
		for (final String splitedCore : cores) {
			//on trim pour le cas, car le split peut avoir laisser des espaces
			final String core = splitedCore.trim();
			solrServerMap.put(core, createSolrServer(core));
		}
	}

	protected abstract SolrServer createSolrServer(String core);

	private <I extends DtObject, R extends DtObject> SolrStatement<I, R> createSolrStatement(final IndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//---------------------------------------------------------------------
		final SolrDocumentCodec<I, R> solrDocumentCodec = new SolrDocumentCodec<>(codecManager);
		return new SolrStatement<>(solrDocumentCodec, getSolrServer(indexDefinition.getName()), obtainIndexFieldNameResolver(indexDefinition));
	}

	/** {@inheritDoc} */
	public final void registerIndexFieldNameResolver(final IndexDefinition indexDefinition, final IndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(indexFieldNameResolver);
		//---------------------------------------------------------------------
		indexFieldNameResolverMap.put(indexDefinition.getName(), indexFieldNameResolver);
	}

	/**
	 * Fournit l' IndexFieldNameResolver d'un index.
	 * @param indexDefinition IndexDefinition de l'index
	 * @return IndexFieldNameResolver associé à l'index
	 */
	protected final IndexFieldNameResolver obtainIndexFieldNameResolver(final IndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//---------------------------------------------------------------------
		final IndexFieldNameResolver indexFieldNameResolver = indexFieldNameResolverMap.get(indexDefinition.getName());
		return indexFieldNameResolver != null ? indexFieldNameResolver : DEFAULT_INDEX_FIELD_NAME_RESOLVER;
	}

	/** {@inheritDoc} */
	public final <I extends DtObject, R extends DtObject> void putAll(final IndexDefinition indexDefinition, final Collection<Index<I, R>> indexCollection) {
		Assertion.checkNotNull(indexCollection);
		//---------------------------------------------------------------------
		final SolrStatement<I, R> statement = createSolrStatement(indexDefinition);
		statement.putAll(indexCollection);
		commitSolrServer(indexDefinition);
	}

	private void commitSolrServer(final IndexDefinition indexDefinition) {
		try {
			getSolrServer(indexDefinition.getName()).commit();
		} catch (final Exception e) {
			throw new RuntimeException("Erreur lors du commit solr", e);
		}
	}

	/** {@inheritDoc} */
	public final <I extends DtObject, R extends DtObject> void put(final IndexDefinition indexDefinition, final Index<I, R> index) {
		//On vérifie la cohérence des données SO et SOD.
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(index);
		Assertion.checkArgument(indexDefinition.equals(index.getDefinition()), "les Définitions ne sont pas conformes");
		//---------------------------------------------------------------------
		final SolrStatement<I, R> statement = createSolrStatement(indexDefinition);
		statement.put(index);
		commitSolrServer(indexDefinition);
	}

	/** {@inheritDoc} */
	public final void remove(final IndexDefinition indexDefinition, final URI uri) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(indexDefinition);
		//---------------------------------------------------------------------
		createSolrStatement(indexDefinition).remove(indexDefinition, uri);
		commitSolrServer(indexDefinition);
		markToOptimize(indexDefinition);
	}

	private void markToOptimize(final IndexDefinition indexDefinition) {
		try {
			getSolrServer(indexDefinition.getName()).optimize(true, true, 32); //32 files : empirique
		} catch (final Exception e) {
			throw new RuntimeException("Erreur lors de l'optimize solr", e);
		}
	}

	/** {@inheritDoc} */
	public final <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchQuery searchQuery, final FacetedQuery facetedQuery) {
		Assertion.checkNotNull(searchQuery);
		Assertion.checkNotNull(facetedQuery);
		//---------------------------------------------------------------------
		final SolrStatement<DtObject, R> statement = createSolrStatement(searchQuery.getIndexDefinition());
		return statement.loadList(searchQuery, facetedQuery, rowsPerQuery);
	}

	/** {@inheritDoc} */
	public final long count(final IndexDefinition indexDefinition) {
		Assertion.checkNotNull(indexDefinition);
		//---------------------------------------------------------------------
		return createSolrStatement(indexDefinition).count();
	}

	/** {@inheritDoc} */
	public final void remove(final IndexDefinition indexDefinition, final ListFilter listFilter) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(listFilter);
		//---------------------------------------------------------------------
		createSolrStatement(indexDefinition).remove(indexDefinition, listFilter);
		commitSolrServer(indexDefinition);
		markToOptimize(indexDefinition);
	}

	/**
	 * Récupération des indexes SOLR perçus comme autant de serveurs.
	 * @param core Index SOLR/Lucene 
	 * @return Serveur Solr mappant une index SOLR/Lucene
	 */
	private SolrServer getSolrServer(final String core) {
		Assertion.checkArgNotEmpty(core);
		//---------------------------------------------------------------------
		final SolrServer solrServer = solrServerMap.get(core);
		Assertion.checkNotNull(solrServer, "Aucun solrServer trouvé pour le core {0}", core);
		return solrServer;
	}
}
