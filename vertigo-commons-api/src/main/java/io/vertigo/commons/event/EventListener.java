package io.vertigo.commons.event;

import java.io.Serializable;

/**
 * EventsListener.
 * @author npiedeloup
 * @param <P> Payload's type
 */
public interface EventListener<P extends Serializable> {

	/**
	 * Call when registered channel received an event.
	 * This event is call by a ExecutorService, without thread context, this method should not lock.
	 *
	 * @param event Received event
	 */
	void onEvent(Event<P> event);

}
