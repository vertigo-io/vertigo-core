package io.vertigo.dynamo.impl.persistence.datastore;

import io.vertigo.dynamo.transaction.VTransactionResource;
import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Transactionnal event launcher.
 * @author npiedeloup
 */
public final class EventTransactionResource implements VTransactionResource {
	private final Map<String, Set<EventListener>> listenersPerTopic = new HashMap<>();
	private final Map<String, Set<String>> eventsPerTopic = new HashMap<>();

	private enum State {
		Started, Closed
	}

	private State state = State.Started;

	/** {@inheritDoc} */
	@Override
	public void commit() {
		check();
		for (final Entry<String, Set<String>> eventEntry : eventsPerTopic.entrySet()) {
			for (final EventListener listener : listenersPerTopic.get(eventEntry.getKey())) {
				for (final String eventPayload : eventEntry.getValue()) {
					listener.onEvent(eventPayload);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		check();
		eventsPerTopic.clear();
		listenersPerTopic.clear();
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

	public void fire(final String topic, final String event) {
		check();
		obtainEvents(topic).add(event);
	}

	public void subscribe(final String topic, final EventListener listener) {
		check();
		obtainListeners(topic).add(listener);
	}

	private Set<String> obtainEvents(final String topic) {
		Set<String> events = eventsPerTopic.get(topic);
		if (events == null) {
			events = new HashSet<>();
			eventsPerTopic.put(topic, events);
		}
		return events;
	}

	private Set<EventListener> obtainListeners(final String topic) {
		Set<EventListener> listeners = listenersPerTopic.get(topic);
		if (listeners == null) {
			listeners = new HashSet<>();
			listenersPerTopic.put(topic, listeners);
		}
		return listeners;
	}

}
