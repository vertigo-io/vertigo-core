package io.vertigo.dynamo.events;

import io.vertigo.lang.Component;

import java.io.Serializable;

/**
 * Inter-components events manager.
 * Producer/Consumer on channel for communication between components.
 * When registering to channel, Listeners are configured to listen locally or globally over system.
 *
 * Example :
 * A cache component should listen globally : a modification in one app should flush cache all over the system
 * An audit component should listen locally to ensure only one app do the audit log
 *
 * @author pchretien, npiedeloup
 */
public interface EventsManager extends Component {

	/**
	 * Fire an event on a channel.
	 * @param channel ChannelName to send event to
	 * @param payload event's payload
	 */
	<P extends Serializable> void fire(EventChannel<P> channel, P payload);

	/**
	 * Register a new listener for this channel.
	 * @param channel ChannelName to listen
	 * @param localOnly If this listener is local sent event only
	 * @param eventsListener EventsListener
	 */
	<P extends Serializable> void register(EventChannel<P> channel, boolean localOnly, EventsListener<P> eventsListener);

}
