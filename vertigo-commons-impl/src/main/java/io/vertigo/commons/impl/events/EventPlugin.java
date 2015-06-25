package io.vertigo.commons.impl.events;

import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventChannel;
import io.vertigo.commons.event.EventListener;
import io.vertigo.lang.Plugin;

import java.io.Serializable;

/**
 * @author pchretien
 */
public interface EventPlugin extends Plugin {

	/**
	 * Emit an event on a channel.
	 * @param <P> Payload type
	 * @param channel ChannelName to send event to
	 * @param event event
	 */
	<P extends Serializable> void emit(EventChannel<P> channel, Event<P> event);

	/**
	 * Register a new listener for this channel.
	 * @param <P> Payload type
	 * @param channel ChannelName to listen
	 * @param eventsListener EventsListener
	 */
	<P extends Serializable> void register(EventChannel<P> channel, EventListener<P> eventsListener);
}
