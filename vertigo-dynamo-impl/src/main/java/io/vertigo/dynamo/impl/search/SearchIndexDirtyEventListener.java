package io.vertigo.dynamo.impl.search;

import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventListener;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.SearchManager;

import java.util.Collections;
import java.util.List;

/**
 * Declare index dirty on event.
 * @author npiedeloup
 */
final class SearchIndexDirtyEventListener implements EventListener<URI> {

	private final SearchManager searchManager;

	/**
	 * Constructor.
	 * @param searchManager SearchManager
	 */
	SearchIndexDirtyEventListener(final SearchManager searchManager) {
		this.searchManager = searchManager;
	}

	/** {@inheritDoc} */
	@Override
	public void onEvent(final Event<URI> event) {
		final URI uri = event.getPayload();
		final List<URI<? extends KeyConcept>> list = Collections.<URI<? extends KeyConcept>> singletonList(uri);
		searchManager.markAsDirty(list);
	}
}
