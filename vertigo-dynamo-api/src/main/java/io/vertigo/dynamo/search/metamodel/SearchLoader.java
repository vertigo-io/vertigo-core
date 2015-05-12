package io.vertigo.dynamo.search.metamodel;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.model.SearchIndex;

import java.util.List;

/**
 * Specific SearchIndex loader.
 * @param <S> Subject
 * @param <I> Indexed data's type
 * @param <R> Result data's type
 * @author npiedeloup, pchretien
 */
public interface SearchLoader<S extends DtSubject, I extends DtObject, R extends DtObject> {
	/**
	 * Load all data from a list of subjects.
	 * @param uris List of subject uris
	 * @return List of searchIndex
	 */
	List<SearchIndex<S, I, R>> loadData(List<URI<S>> uris);

	/**
	 * Create a chunk iterator for crawl all subject data.
	 * @param subjectClass Subject class
	 * @return Iterator of chunk
	 */
	Iterable<SearchChunk<S>> chunk(final Class<S> subjectClass);
}
