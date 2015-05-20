package io.vertigo.dynamo.events;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.util.UUID;

/**
 * Event builder.
 * @author pchretien
 */
public final class EventBuilder implements Builder<Event> {
	private UUID myUuid;
	private String myPayload;

	/**
	 * Fix the payload.
	 * @param payload event's payload
	 * @return this builder
	 */
	public EventBuilder withPayload(final String payload) {
		Assertion.checkArgument(myPayload == null, "payload already set");
		Assertion.checkArgNotEmpty(payload);
		//-----
		myPayload = payload;
		return this;
	}

	/**
	 * Fix the uuid (use by EventManagerImpl only)
	 * @param uuid event's uuid
	 * @return this builder
	 */
	public EventBuilder withUUID(final UUID uuid) {
		Assertion.checkArgument(myUuid == null, "uuid already set");
		Assertion.checkNotNull(uuid);
		//-----
		myUuid = uuid;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Event build() {
		myUuid = myUuid == null ? UUID.randomUUID() : myUuid;
		return new Event(myUuid, myPayload);
	}
}
