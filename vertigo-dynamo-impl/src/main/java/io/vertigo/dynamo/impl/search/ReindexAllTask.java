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

final class ReindexAllTask<S extends KeyConcept> implements Runnable {
	private static boolean REINDEXATION_IN_PROGRESS = false;
	private final SearchIndexDefinition searchIndexDefinition;
	private final SearchManager searchManager;
	private final VTransactionManager transactionManager;

	public ReindexAllTask(final SearchIndexDefinition searchIndexDefinition, final SearchManager searchManager, final VTransactionManager transactionManager) {
		Assertion.checkNotNull(searchIndexDefinition);
		Assertion.checkNotNull(searchManager);
		Assertion.checkNotNull(transactionManager);
		//-----
		this.searchIndexDefinition = searchIndexDefinition;
		this.searchManager = searchManager;
		this.transactionManager = transactionManager;
	}

	@Override
	public void run() {
		Assertion.checkState(!REINDEXATION_IN_PROGRESS, "A reindexation is already in progress");
		//-----
		REINDEXATION_IN_PROGRESS = true;
		try {
			final Class<S> keyConceptClass = (Class<S>) ClassUtil.classForName(searchIndexDefinition.getKeyConceptDtDefinition().getClassCanonicalName(), KeyConcept.class);
			final SearchLoader<S, DtObject> searchLoader = Home.getComponentSpace().resolve(searchIndexDefinition.getSearchLoaderId(), SearchLoader.class);
			String lastUri = "*";
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
				final URI<S> chunkMaxUri = uris.get(uris.size() - 1);
				final String maxUri = String.valueOf(chunkMaxUri.getId());
				searchManager.removeAll(searchIndexDefinition, urisRangeToListFilter(lastUri, maxUri));
				searchManager.putAll(searchIndexDefinition, searchIndexes);
				lastUri = maxUri;
			}

			//On ne retire pas la fin, il y a un risque de retirer les données ajoutées depuis le démarrage de l'indexation
			//TODO : à valider
		} finally {
			REINDEXATION_IN_PROGRESS = false;
		}
	}

	private ListFilter urisRangeToListFilter(final String firstUri, final String lastUri) {
		final String indexIdFieldName = searchIndexDefinition.getIndexDtDefinition().getIdField().get().getName();
		final StringBuilder sb = new StringBuilder();
		sb.append(indexIdFieldName).append(":[");
		sb.append(firstUri).append(" TO ").append(lastUri);
		sb.append("]");
		return new ListFilter(sb.toString());
	}
}
