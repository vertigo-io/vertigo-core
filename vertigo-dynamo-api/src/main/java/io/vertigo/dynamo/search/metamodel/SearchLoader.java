package io.vertigo.dynamo.search.metamodel;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;

import java.util.List;

public interface SearchLoader<S extends DtSubject, I extends DtObject> {
	/**
	 * Load all data from a list of subjects.
	 */
	List<I> loadData(List<URI<S>> uris);
}
