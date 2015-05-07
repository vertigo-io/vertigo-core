package io.vertigo.dynamo.search.metamodel;

import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;

import java.util.List;

/**
 * Chunk of subject uris.
 * @author npiedeloup, pchretien
 * @param <S> Subject's type
 */
public interface SearchChunk<S extends DtSubject> {

	/**
	 * @return All Subject's uris of this chunk
	 */
	List<URI<S>> getAllURIs();
}
