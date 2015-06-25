package io.vertigo.dynamo.impl.persistence.datastore.cache;

import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventListener;
import io.vertigo.dynamo.domain.model.URI;

/**
 * Clear cache eventListener.
 * @author npiedeloup
 */
final class CacheClearEventListener implements EventListener<URI> {

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
