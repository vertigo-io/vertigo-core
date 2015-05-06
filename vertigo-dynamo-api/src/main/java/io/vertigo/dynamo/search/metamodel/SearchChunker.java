package io.vertigo.dynamo.search.metamodel;

import io.vertigo.dynamo.domain.model.DtSubject;

import java.util.Iterator;

public final class SearchChunker {
	//range, date
	<S extends DtSubject> Iterator<SearchChunk<S>> chunk(final Class<S> subjectClass) {

	}
}
