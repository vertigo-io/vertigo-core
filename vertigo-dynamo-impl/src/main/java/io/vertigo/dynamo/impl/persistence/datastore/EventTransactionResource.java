package io.vertigo.dynamo.impl.persistence.datastore;

import io.vertigo.dynamo.events.EventChannel;
import io.vertigo.dynamo.events.EventsManager;
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
	private final EventsManager eventsManager;

	private enum State {
		Started, Closed
	}

	private State state = State.Started;

	/**
	 * Constructor.
	 * @param eventsManager EventsManager
	 */
	EventTransactionResource(final EventsManager eventsManager) {
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
