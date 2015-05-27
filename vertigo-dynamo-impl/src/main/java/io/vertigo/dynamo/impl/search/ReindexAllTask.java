package io.vertigo.dynamo.impl.search;

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.metamodel.SearchLoader;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.util.ClassUtil;

import java.util.Collection;

final class ReindexAllTask<S extends DtSubject> implements Runnable {

	private final SearchIndexDefinition searchIndexDefinition;
	private final SearchManager searchManager;

	public ReindexAllTask(final SearchIndexDefinition searchIndexDefinition, final SearchManager searchManager) {
		this.searchIndexDefinition = searchIndexDefinition;
		this.searchManager = searchManager;
	}

	@Override
	public void run() {
		final Class<S> subjectClass = (Class<S>) ClassUtil.classForName(searchIndexDefinition.getSubjectDtDefinition().getClassCanonicalName(), DtSubject.class);
		final SearchLoader<S, DtObject> searchLoader = Home.getComponentSpace().resolve(searchIndexDefinition.getSearchLoaderId(), SearchLoader.class);
		for (final SearchChunk<S> searchChunk : searchLoader.chunk(subjectClass)) {
			final Collection<SearchIndex<S, DtObject>> searchIndexes = searchLoader.loadData(searchChunk.getAllURIs());
			searchManager.putAll(searchIndexDefinition, searchIndexes);
		}
	}
}
