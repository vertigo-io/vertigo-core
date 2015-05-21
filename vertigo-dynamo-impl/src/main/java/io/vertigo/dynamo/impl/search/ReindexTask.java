package io.vertigo.dynamo.impl.search;

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.metamodel.SearchLoader;
import io.vertigo.dynamo.search.model.SearchIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class ReindexTask implements Runnable {

	private final SearchIndexDefinition searchIndexDefinition;
	private final List<URI<? extends DtSubject>> dirtyElements;
	private final SearchManager searchManager;

	public ReindexTask(final SearchIndexDefinition searchIndexDefinition, final List<URI<? extends DtSubject>> dirtyElements, final SearchManager searchManager) {
		this.searchIndexDefinition = searchIndexDefinition;
		this.dirtyElements = dirtyElements;//On ne fait pas la copie ici
		this.searchManager = searchManager;
	}

	@Override
	public void run() {
		final List<URI<? extends DtSubject>> reindexUris;
		synchronized (dirtyElements) {
			reindexUris = new ArrayList<>(dirtyElements);
			dirtyElements.clear();
		}
		final SearchLoader searchLoader = Home.getComponentSpace().resolve(searchIndexDefinition.getSearchLoaderClass());
		final Collection<SearchIndex<DtSubject, DtObject>> searchIndexes = searchLoader.loadData(reindexUris);
		searchManager.putAll(searchIndexDefinition, searchIndexes);
	}
}
