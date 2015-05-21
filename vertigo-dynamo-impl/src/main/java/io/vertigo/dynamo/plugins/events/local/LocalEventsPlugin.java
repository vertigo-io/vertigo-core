package io.vertigo.dynamo.plugins.events.local;

import io.vertigo.dynamo.events.Event;
import io.vertigo.dynamo.events.EventChannel;
import io.vertigo.dynamo.events.EventsListener;
import io.vertigo.dynamo.impl.events.EventsPlugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author npiedeloup
 */
public final class LocalEventsPlugin implements EventsPlugin {
	private final Map<EventChannel<?>, Set<EventsListener<?>>> listenersPerChannel = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public <P extends Serializable> void emit(final EventChannel<P> channel, final Event<P> event) {
		final Set<EventsListener<P>> eventsListeners = this.<P> obtainEventsListeners(channel);
		for (final EventsListener<P> eventsListener : eventsListeners) {
			eventsListener.onEvent(event);
			//TODO gestion d'erreur ?
		}
	}

	/** {@inheritDoc} */
	@Override
	public <P extends Serializable> void register(final EventChannel<P> channel, final EventsListener<P> eventsListener) {
		final Set<EventsListener<P>> eventsListeners = this.<P> obtainEventsListeners(channel);
		eventsListeners.add(eventsListener);
	}

	private <P extends Serializable> Set<EventsListener<P>> obtainEventsListeners(final EventChannel<P> channel) {
		Set listeners = listenersPerChannel.get(channel);
		if (listeners == null) {
			listeners = new HashSet<>();
			listenersPerChannel.put(channel, listeners);
		}
		return listeners;
	}
}
