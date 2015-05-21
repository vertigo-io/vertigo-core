package io.vertigo.dynamo.impl.events;

import io.vertigo.dynamo.events.Event;
import io.vertigo.dynamo.events.EventChannel;
import io.vertigo.dynamo.events.EventsListener;
import io.vertigo.lang.Plugin;

import java.io.Serializable;

/**
 * @author pchretien
 */
public interface EventsPlugin extends Plugin {

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
	<P extends Serializable> void register(EventChannel<P> channel, EventsListener<P> eventsListener);
}
