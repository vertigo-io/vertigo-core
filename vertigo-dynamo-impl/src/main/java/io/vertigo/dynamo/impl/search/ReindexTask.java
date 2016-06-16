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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import io.vertigo.app.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.metamodel.SearchLoader;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.lang.Assertion;

final class ReindexTask implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(ReindexTask.class);
	private static final int DIRTY_ELEMENTS_CHUNK_SIZE = 500;
	private static final int REINDEX_ERROR_WAIT = 1000; // attend 1s avant de recommencer
	private static final int REINDEX_ERROR_MAX_RETRY = 5; //il y a 5 + 1 essais au total (le premier + 5 retry)

	private final SearchIndexDefinition searchIndexDefinition;
	private final List<URI<? extends KeyConcept>> dirtyElements;
	private final SearchManager searchManager;

	private final VTransactionManager transactionManager;

	ReindexTask(final SearchIndexDefinition searchIndexDefinition, final List<URI<? extends KeyConcept>> dirtyElements, final SearchManager searchManager, final VTransactionManager transactionManager) {
		Assertion.checkNotNull(searchIndexDefinition);
		Assertion.checkNotNull(dirtyElements);
		Assertion.checkNotNull(searchManager);
		Assertion.checkNotNull(transactionManager);
		//-----
		this.searchIndexDefinition = searchIndexDefinition;
		this.dirtyElements = dirtyElements; //On ne fait pas la copie ici
		this.searchManager = searchManager;
		this.transactionManager = transactionManager;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		long dirtyElementsCount = 0;
		do {
			final long startTime = System.currentTimeMillis();
			final List<URI<? extends KeyConcept>> reindexUris = new ArrayList<>();
			try {
				synchronized (dirtyElements) {
					if (!dirtyElements.isEmpty()) {
						reindexUris.addAll(dirtyElements.subList(0, Math.min(dirtyElements.size(), DIRTY_ELEMENTS_CHUNK_SIZE)));
						dirtyElements.removeAll(reindexUris);
					}
				}
				dirtyElementsCount = reindexUris.size();
				if (!reindexUris.isEmpty()) {
					loadAndIndexAndRetry(reindexUris, 0);
				}
			} catch (final Exception e) {
				LOGGER.error("Update index error, skip " + dirtyElementsCount + " elements (" + reindexUris + ")", e);
			} finally {
				LOGGER.info("Update index, " + dirtyElementsCount + " " + searchIndexDefinition.getName() + " finished in " + (System.currentTimeMillis() - startTime) + "ms");
			}
		} while (dirtyElementsCount > 0);

	}

	private void loadAndIndexAndRetry(final List<URI<? extends KeyConcept>> reindexUris, final int tryNumber) {
		try {
			loadAndIndex(reindexUris);
		} catch (final Exception e) {
			if (tryNumber >= REINDEX_ERROR_MAX_RETRY) {
				LOGGER.error("Update index error after " + tryNumber + " retry", e);
				throw e;
			}
			//Sinon on attend et on retry
			LOGGER.warn("Update index error, will retry " + (REINDEX_ERROR_MAX_RETRY - tryNumber) + " time, in " + REINDEX_ERROR_WAIT + " ms", e);
			try {
				Thread.sleep(REINDEX_ERROR_WAIT);
			} catch (final InterruptedException ie) {
				//rien
			}
			loadAndIndexAndRetry(reindexUris, tryNumber + 1); //on retry
		}
	}

	private void loadAndIndex(final List<URI<? extends KeyConcept>> reindexUris) {
		final SearchLoader searchLoader = Home.getApp().getComponentSpace().resolve(searchIndexDefinition.getSearchLoaderId(), SearchLoader.class);
		final Collection<SearchIndex<KeyConcept, DtObject>> searchIndexes;

		// >>> Tx start
		try (final VTransactionWritable tx = transactionManager.createCurrentTransaction()) { //on execute dans une transaction
			searchIndexes = searchLoader.loadData(reindexUris);
		}
		// <<< Tx end
		removedNotFoundKeyConcept(searchIndexes, reindexUris);
		if (!searchIndexes.isEmpty()) {
			searchManager.putAll(searchIndexDefinition, searchIndexes);
		}
	}

	private void removedNotFoundKeyConcept(final Collection<SearchIndex<KeyConcept, DtObject>> searchIndexes, final List<URI<? extends KeyConcept>> reindexUris) {
		if (searchIndexes.size() < reindexUris.size()) {
			final Set<URI<? extends KeyConcept>> notFoundUris = new LinkedHashSet<>(reindexUris);
			for (final SearchIndex<KeyConcept, DtObject> searchIndex : searchIndexes) {
				if (notFoundUris.contains(searchIndex.getURI())) {
					notFoundUris.remove(searchIndex.getURI());
				}
			}
			searchManager.removeAll(searchIndexDefinition, urisToListFilter(notFoundUris));
		}
	}

	private ListFilter urisToListFilter(final Set<URI<? extends KeyConcept>> removedUris) {
		final String indexIdFieldName = searchIndexDefinition.getIndexDtDefinition().getIdField().get().getName();
		final StringBuilder sb = new StringBuilder();
		sb.append(indexIdFieldName).append(":(");
		String sep = "";
		for (final URI<?> uri : removedUris) {
			sb.append(sep);
			sb.append(String.valueOf(uri.getId()));
			sep = " OR ";
		}
		sb.append(")");
		return new ListFilter(sb.toString());
	}
}
