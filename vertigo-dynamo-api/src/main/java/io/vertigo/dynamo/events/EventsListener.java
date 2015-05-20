package io.vertigo.dynamo.events;

import java.io.Serializable;

/**
 * EventsListener.
 * @author npiedeloup
 */
public interface EventsListener<P extends Serializable> {

	/**
	 * Call when registered channel received an event.
	 * This event is call by a ExecutorService, without thread context, this method should not lock.
	 *
	 * @param event Received event
	 */
	void onEvent(Event<P> event);

}
