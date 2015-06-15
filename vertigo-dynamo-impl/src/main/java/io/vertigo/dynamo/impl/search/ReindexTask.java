package io.vertigo.dynamo.impl.search;

import io.vertigo.core.Home;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class ReindexTask implements Runnable {

	private final SearchIndexDefinition searchIndexDefinition;
	private final List<URI<? extends KeyConcept>> dirtyElements;
	private final SearchManager searchManager;

	private final VTransactionManager transactionManager;

	public ReindexTask(final SearchIndexDefinition searchIndexDefinition, final List<URI<? extends KeyConcept>> dirtyElements, final SearchManager searchManager, final VTransactionManager transactionManager) {
		Assertion.checkNotNull(searchIndexDefinition);
		Assertion.checkNotNull(dirtyElements);
		Assertion.checkNotNull(searchManager);
		Assertion.checkNotNull(transactionManager);
		//-----
		this.searchIndexDefinition = searchIndexDefinition;
		this.dirtyElements = dirtyElements;//On ne fait pas la copie ici
		this.searchManager = searchManager;
		this.transactionManager = transactionManager;
	}

	@Override
	public void run() {
		final List<URI<? extends KeyConcept>> reindexUris;
		synchronized (dirtyElements) {
			reindexUris = new ArrayList<>(dirtyElements);
			dirtyElements.clear();
		}
		if (!reindexUris.isEmpty()) {
			final SearchLoader searchLoader = Home.getComponentSpace().resolve(searchIndexDefinition.getSearchLoaderId(), SearchLoader.class);
			final Collection<SearchIndex<KeyConcept, DtObject>> searchIndexes;

			// >>> Tx start
			try (final VTransactionWritable tx = transactionManager.createCurrentTransaction()) { //on execute dans une transaction
				searchIndexes = searchLoader.loadData(reindexUris);
			}
			// <<< Tx end

			removedNotFoundKeyConcept(searchIndexes, reindexUris);
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
