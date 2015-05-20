package io.vertigo.dynamo.impl.persistence.datastore.cache;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.events.Event;
import io.vertigo.dynamo.events.EventsListener;

/**
 * Clear cache eventListener.
 * @author npiedeloup
 */
final class CacheClearEventListener implements EventsListener<URI> {

	private final CacheDataStore cacheDataStore;

	/**
	 * Constructor.
	 * @param cacheDataStore CacheDataStore
	 */
	CacheClearEventListener(final CacheDataStore cacheDataStore) {
		this.cacheDataStore = cacheDataStore;
	}

	/** {@inheritDoc} */
	@Override
	public void onEvent(final Event<URI> event) {
		final URI uri = event.getPayload();
		cacheDataStore.clearCache(uri.getDefinition());
	}
}
