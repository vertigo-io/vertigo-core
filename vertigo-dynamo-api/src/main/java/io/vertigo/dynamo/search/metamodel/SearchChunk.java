package io.vertigo.dynamo.search.metamodel;

import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;

import java.util.List;

/**
 * Chunk of keyConcept uris.
 * @author npiedeloup, pchretien
 * @param <S> KeyConcept's type
 */
public interface SearchChunk<S extends KeyConcept> {

	/**
	 * @return All KeyConcept's uris of this chunk
	 */
	List<URI<S>> getAllURIs();
}
