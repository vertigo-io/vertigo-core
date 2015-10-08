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
package io.vertigo.dynamo.impl.search;

import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.metamodel.SearchLoader;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Reindex all data taks.
 * @author npiedeloup (27 juil. 2015 14:35:14)
 * @param <S> KeyConcept type
 */
final class ReindexAllTask<S extends KeyConcept> implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(ReindexAllTask.class);
	private static volatile boolean REINDEXATION_IN_PROGRESS = false;
	private static volatile long REINDEX_COUNT = 0;
	private final WritableFuture<Long> reindexFuture;
	private final SearchIndexDefinition searchIndexDefinition;
	private final SearchManager searchManager;
	private final VTransactionManager transactionManager;

	/**
	 * Constructor.
	 * @param searchIndexDefinition Search index definition
	 * @param reindexFuture Future for result
	 * @param searchManager Search manager
	 * @param transactionManager Transaction manager
	 */
	ReindexAllTask(final SearchIndexDefinition searchIndexDefinition, final WritableFuture<Long> reindexFuture, final SearchManager searchManager, final VTransactionManager transactionManager) {
		Assertion.checkNotNull(searchIndexDefinition);
		Assertion.checkNotNull(reindexFuture);
		Assertion.checkNotNull(searchManager);
		Assertion.checkNotNull(transactionManager);
		//-----
		this.searchIndexDefinition = searchIndexDefinition;
		this.reindexFuture = reindexFuture;
		this.searchManager = searchManager;
		this.transactionManager = transactionManager;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		if (isReindexInProgress()) {
			final String warnMessage = "Reindexation of " + searchIndexDefinition.getName() + " is already in progess (" + getReindexCount() + " elements done)";
			LOGGER.warn(warnMessage);
			reindexFuture.fail(new RuntimeException(warnMessage));
		} else {
			//-----
			startReindex();
			long reindexCount = 0;
			final long startTime = System.currentTimeMillis();
			try {
				final Class<S> keyConceptClass = (Class<S>) ClassUtil.classForName(searchIndexDefinition.getKeyConceptDtDefinition().getClassCanonicalName(), KeyConcept.class);
				final SearchLoader<S, DtObject> searchLoader = Home.getComponentSpace().resolve(searchIndexDefinition.getSearchLoaderId(), SearchLoader.class);
				String lastUri = "*";
				LOGGER.info("Reindexation of " + searchIndexDefinition.getName() + " started");
				for (final Iterator<SearchChunk<S>> it = searchLoader.chunk(keyConceptClass).iterator(); it.hasNext();) {
					final SearchChunk<S> searchChunk;
					// >>> Tx start
					try (final VTransactionWritable tx = transactionManager.createCurrentTransaction()) { //on execute dans une transaction
						searchChunk = it.next();
					}
					// <<< Tx end

					final List<URI<S>> uris = searchChunk.getAllURIs();
					Assertion.checkArgument(!uris.isEmpty(), "The uris list of a SearchChunk can't be empty");
					//-----
					final Collection<SearchIndex<S, DtObject>> searchIndexes;
					// >>> Tx start
					try (final VTransactionWritable tx = transactionManager.createCurrentTransaction()) { //on execute dans une transaction
						searchIndexes = searchLoader.loadData(uris);
					}
					// <<< Tx end
					reindexCount += uris.size();
					updateReindexCount(reindexCount);
					final URI<S> chunkMaxUri = uris.get(uris.size() - 1);
					final String maxUri = String.valueOf(chunkMaxUri.getId());
					searchManager.removeAll(searchIndexDefinition, urisRangeToListFilter(lastUri, maxUri));
					if (!searchIndexes.isEmpty()) {
						searchManager.putAll(searchIndexDefinition, searchIndexes);
					}
					lastUri = maxUri;
				}
				//On ne retire pas la fin, il y a un risque de retirer les données ajoutées depuis le démarrage de l'indexation
				reindexFuture.success(reindexCount);
			} catch (final Exception e) {
				LOGGER.error("Reindexation error", e);
				reindexFuture.fail(e);
			} finally {
				stopReindex();
				LOGGER.info("Reindexation of " + searchIndexDefinition.getName() + " finished in " + (System.currentTimeMillis() - startTime) + "ms (" + reindexCount + " elements done)");
			}
		}
	}

	private static boolean isReindexInProgress() {
		return REINDEXATION_IN_PROGRESS;
	}

	private static void startReindex() {
		REINDEXATION_IN_PROGRESS = true;
	}

	private static void stopReindex() {
		REINDEXATION_IN_PROGRESS = false;
	}

	private static void updateReindexCount(final long reindexCount) {
		REINDEX_COUNT = reindexCount;
	}

	private static long getReindexCount() {
		return REINDEX_COUNT;
	}

	private ListFilter urisRangeToListFilter(final String firstUri, final String lastUri) {
		final String indexIdFieldName = searchIndexDefinition.getIndexDtDefinition().getIdField().get().getName();
		final StringBuilder sb = new StringBuilder();
		sb.append(indexIdFieldName).append(":{");
		sb.append(firstUri).append(" TO ").append(lastUri);
		sb.append("]");
		return new ListFilter(sb.toString());
	}
}
