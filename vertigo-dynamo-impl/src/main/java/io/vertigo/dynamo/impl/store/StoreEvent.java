package io.vertigo.dynamo.impl.store;

import io.vertigo.commons.eventbus.Event;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.lang.Assertion;

/**
 * This class defines the event that is emitted when the store deals with an object identified by an uri.
 * 
 * @author pchretien
 */
public final class StoreEvent implements Event {
	/**
	 * Type of event.
	 */
	public static enum Type {
		Create,
		Update,
		Delete
	}

	private final Type type;
	private final URI uri;

	public StoreEvent(final Type type, final URI uri) {
		Assertion.checkNotNull(type);
		Assertion.checkNotNull(uri);
		//-----
		this.type = type;
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
	}

	public Type getType() {
		return type;
	}
}
