package io.vertigo.commons.event;

import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author pchretien
 * @param <P> Type of payload
 */
public final class Event<P extends Serializable> {
	private final UUID uuid;
	private final P payload;

	/**
	 * @param uuid UUID
	 * @param payload Payload
	 */
	Event(final UUID uuid, final P payload) {
		Assertion.checkNotNull(uuid);
		Assertion.checkNotNull(payload);
		//-----
		this.uuid = uuid;
		this.payload = payload;
	}

	/**
	 * @return This event's uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * @return This event's payload
	 */
	public P getPayload() {
		return payload;
	}
}
