package io.vertigo.commons.impl.event;

import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventBuilder;
import io.vertigo.commons.event.EventChannel;
import io.vertigo.commons.event.EventListener;
import io.vertigo.commons.event.EventManager;
import io.vertigo.commons.plugins.event.local.LocalEventsPlugin;

import java.io.Serializable;

/**
 * @author pchretien, npiedeloup
 */
public final class EventManagerImpl implements EventManager {
	private final EventPlugin localEventsPlugin = new LocalEventsPlugin();

	@Override
	public <P extends Serializable> void fire(final EventChannel<P> channel, final P payload) {
		final Event<P> event = new EventBuilder().withPayload(payload).build();
		localEventsPlugin.emit(channel, event);
	}

	/**
	 * Register a new listener for this channel.
	 * @param channel ChannelName to listen
	 * @param eventsListener EventsListener
	 */
	@Override
	public <P extends Serializable> void register(final EventChannel<P> channel, final EventListener<P> eventsListener) {
		localEventsPlugin.register(channel, eventsListener);
	}
}
