package io.vertigo.dynamo.search.metamodel;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.model.SearchIndex;

import java.util.List;

/**
 * Specific SearchIndex loader.
 * @param <K> KeyConcept
 * @param <I> Indexed data's type
 * @author npiedeloup, pchretien
 */
public interface SearchLoader<K extends KeyConcept, I extends DtObject> {
	/**
	 * Load all data from a list of keyConcepts.
	 * @param uris List of keyConcept uris
	 * @return List of searchIndex
	 */
	List<SearchIndex<K, I>> loadData(List<URI<K>> uris);

	/**
	 * Create a chunk iterator for crawl all keyConcept data.
	 * @param keyConceptClass keyConcept class
	 * @return Iterator of chunk
	 */
	Iterable<SearchChunk<K>> chunk(final Class<K> keyConceptClass);
}
