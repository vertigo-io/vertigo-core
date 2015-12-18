package io.vertigo.commons.impl.eventbus;

import io.vertigo.commons.eventbus.Event;
import io.vertigo.commons.eventbus.EventListener;
import io.vertigo.lang.Assertion;

final class EventBusSubscription<E extends Event> {
	private final Class<E> eventType;
	private final EventListener<E> eventListener;

	EventBusSubscription(final Class<E> eventType, final EventListener<E> eventListener) {
		this.eventType = eventType;
		this.eventListener = eventListener;
	}

	boolean accept(final Event event) {
		Assertion.checkNotNull(event);
		//-----
		return eventType.isInstance(event);
	}

	EventListener<E> getListener() {
		return eventListener;
	}
}
