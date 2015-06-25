package io.vertigo.commons.plugins.event.local;

import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventChannel;
import io.vertigo.commons.event.EventListener;
import io.vertigo.commons.impl.events.EventPlugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author npiedeloup
 */
public final class LocalEventsPlugin implements EventPlugin {
	private final Map<EventChannel<?>, Set<EventListener<?>>> listenersPerChannel = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public <P extends Serializable> void emit(final EventChannel<P> channel, final Event<P> event) {
		final Set<EventListener<P>> eventsListeners = this.<P> obtainEventsListeners(channel);
		for (final EventListener<P> eventsListener : eventsListeners) {
			eventsListener.onEvent(event);
			//TODO gestion d'erreur ?
		}
	}

	/** {@inheritDoc} */
	@Override
	public <P extends Serializable> void register(final EventChannel<P> channel, final EventListener<P> eventsListener) {
		final Set<EventListener<P>> eventsListeners = this.<P> obtainEventsListeners(channel);
		eventsListeners.add(eventsListener);
	}

	private <P extends Serializable> Set<EventListener<P>> obtainEventsListeners(final EventChannel<P> channel) {
		Set listeners = listenersPerChannel.get(channel);
		if (listeners == null) {
			listeners = new HashSet<>();
			listenersPerChannel.put(channel, listeners);
		}
		return listeners;
	}
}
