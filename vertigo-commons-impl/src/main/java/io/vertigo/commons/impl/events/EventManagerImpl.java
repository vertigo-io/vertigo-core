package io.vertigo.commons.impl.events;

import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventBuilder;
import io.vertigo.commons.event.EventChannel;
import io.vertigo.commons.event.EventListener;
import io.vertigo.commons.event.EventManager;
import io.vertigo.commons.plugins.event.local.LocalEventsPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.Serializable;

import javax.inject.Inject;

/**
 * @author pchretien, npiedeloup
 */
public final class EventManagerImpl implements EventManager {
	private final EventPlugin localEventsPlugin = new LocalEventsPlugin();
	private final Option<EventPlugin> remoteEventsPlugin;

	@Inject
	public EventManagerImpl(final Option<EventPlugin> remoteEventsPlugin) {
		Assertion.checkNotNull(remoteEventsPlugin);
		//-----
		this.remoteEventsPlugin = remoteEventsPlugin;
	}

	@Override
	public <P extends Serializable> void fire(final EventChannel<P> channel, final P payload) {
		final Event<P> event = new EventBuilder().withPayload(payload).build();
		localEventsPlugin.emit(channel, event);
		if (remoteEventsPlugin.isDefined()) {
			remoteEventsPlugin.get().emit(channel, event);
		}
	}

	/**
	 * Register a new listener for this channel.
	 * @param channel ChannelName to listen
	 * @param localOnly If this listener is local sent event only
	 * @param eventsListener EventsListener
	 */
	@Override
	public <P extends Serializable> void register(final EventChannel<P> channel, final boolean localOnly, final EventListener<P> eventsListener) {
		if (localOnly || remoteEventsPlugin.isEmpty()) {
			localEventsPlugin.register(channel, eventsListener);
		} else {
			//Can't assert if there is no remotePlugin, because listener component aren't aware of this (ex: PersistanceManager can't determine if there is a remoteEventPlugin)
			remoteEventsPlugin.get().register(channel, eventsListener);
		}
	}
}
