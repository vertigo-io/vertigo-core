package io.vertigo.dynamo.events;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.io.Serializable;
import java.util.UUID;

/**
 * Event builder.
 * @author pchretien
 */
public final class EventBuilder<P extends Serializable> implements Builder<Event<P>> {
	private UUID myUuid;
	private P myPayload;

	/**
	 * Fix the payload.
	 * @param payload event's payload
	 * @return this builder
	 */
	public EventBuilder<P> withPayload(final P payload) {
		Assertion.checkArgument(myPayload == null, "payload already set");
		//-----
		myPayload = payload;
		return this;
	}

	/**
	 * Fix the uuid (use by EventManagerImpl only)
	 * @param uuid event's uuid
	 * @return this builder
	 */
	public EventBuilder<P> withUUID(final UUID uuid) {
		Assertion.checkArgument(myUuid == null, "uuid already set");
		Assertion.checkNotNull(uuid);
		//-----
		myUuid = uuid;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Event<P> build() {
		myUuid = myUuid == null ? UUID.randomUUID() : myUuid;
		return new Event<>(myUuid, myPayload);
	}
}
