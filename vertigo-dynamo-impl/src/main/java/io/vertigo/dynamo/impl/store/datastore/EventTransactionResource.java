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
package io.vertigo.dynamo.impl.store.datastore;

import io.vertigo.commons.event.EventChannel;
import io.vertigo.commons.event.EventManager;
import io.vertigo.dynamo.transaction.VTransactionResource;
import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Transactionnal event launcher.
 * @author npiedeloup
 * @param <P> Type of payload
 */
public final class EventTransactionResource<P extends Serializable> implements VTransactionResource {
	private final Map<EventChannel<P>, Set<P>> eventPayloadsPerChannel = new HashMap<>();
	private final EventManager eventsManager;

	private enum State {
		Started, Closed
	}

	private State state = State.Started;

	/**
	 * Constructor.
	 * @param eventsManager EventsManager
	 */
	EventTransactionResource(final EventManager eventsManager) {
		Assertion.checkNotNull(eventsManager);
		//-----
		this.eventsManager = eventsManager;
	}

	/**
	 * Fire a event on commit.
	 * @param channel Events channel
	 * @param payload Event's payload
	 */
	public void fireOnCommit(final EventChannel<P> channel, final P payload) {
		check();
		obtainEvents(channel).add(payload);
	}

	/** {@inheritDoc} */
	@Override
	public void commit() {
		check();
		for (final Entry<EventChannel<P>, Set<P>> eventEntry : eventPayloadsPerChannel.entrySet()) {
			for (final P eventPayload : eventEntry.getValue()) {
				eventsManager.fire(eventEntry.getKey(), eventPayload);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		check();
		eventPayloadsPerChannel.clear();
		state = State.Closed;
	}

	private void check() {
		Assertion.checkArgument(state == State.Started, "Events already sent");
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() {
		check();
		//Pas de mise à jour
	}

	private Set<P> obtainEvents(final EventChannel<P> channel) {
		Set<P> events = eventPayloadsPerChannel.get(channel);
		if (events == null) {
			events = new HashSet<>();
			eventPayloadsPerChannel.put(channel, events);
		}
		return events;
	}

}
