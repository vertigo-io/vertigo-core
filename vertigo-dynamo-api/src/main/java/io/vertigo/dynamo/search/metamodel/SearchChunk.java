package io.vertigo.dynamo.search.metamodel;

import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;

import java.util.List;

public interface SearchChunk<S extends DtSubject> {

	List<URI<S>> getAllURIs();
}
