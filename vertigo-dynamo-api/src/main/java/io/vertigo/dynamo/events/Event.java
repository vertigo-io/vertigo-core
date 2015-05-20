package io.vertigo.dynamo.events;

import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author pchretien
 */
public final class Event<P extends Serializable> {
	private final UUID uuid;
	private final P payload;

	Event(final UUID uuid, final P payload) {
		Assertion.checkNotNull(uuid);
		Assertion.checkNotNull(payload);
		//-----
		this.uuid = uuid;
		this.payload = payload;
	}

	public UUID getUuid() {
		return uuid;
	}

	public P getPayload() {
		return payload;
	}
}
