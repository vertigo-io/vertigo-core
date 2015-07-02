/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.commons.event;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.io.Serializable;
import java.util.UUID;

/**
 * Event builder.
 * @author pchretien
 * @param <P> Payload's type
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
