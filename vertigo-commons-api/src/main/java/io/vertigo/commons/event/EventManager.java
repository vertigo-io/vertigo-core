package io.vertigo.commons.event;

import io.vertigo.lang.Component;

import java.io.Serializable;

/**
 * Inter-components events manager.
 * Producer/Consumer on channel for communication between components.
 * When registering to channel, Listeners are configured to listen locally only.
 *
 * Example :
 * A cache component should listen : a modification in one app should flush cache all over the system : it's cache component responsibility to do this
 * An audit component should listen to do the audit log
 *
 * @author pchretien, npiedeloup
 */
public interface EventManager extends Component {

	/**
	 * Fire an event on a channel.
	 * @param channel ChannelName to send event to
	 * @param payload event's payload
	 */
	<P extends Serializable> void fire(EventChannel<P> channel, P payload);

	/**
	 * Register a new listener for this channel.
	 * @param channel ChannelName to listen
	 * @param eventsListener EventsListener
	 */
	<P extends Serializable> void register(EventChannel<P> channel, EventListener<P> eventsListener);

}
