package io.vertigo.dynamo.impl.events;

import io.vertigo.dynamo.events.Event;
import io.vertigo.dynamo.events.EventBuilder;
import io.vertigo.dynamo.events.EventChannel;
import io.vertigo.dynamo.events.EventsListener;
import io.vertigo.dynamo.events.EventsManager;
import io.vertigo.dynamo.plugins.events.local.LocalEventsPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.Serializable;

import javax.inject.Inject;

/**
 * @author pchretien, npiedeloup
 */
public final class EventsManagerImpl implements EventsManager {
	private final EventsPlugin localEventsPlugin = new LocalEventsPlugin();
	private final Option<EventsPlugin> remoteEventsPlugin;

	@Inject
	public EventsManagerImpl(final Option<EventsPlugin> remoteEventsPlugin) {
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
	public <P extends Serializable> void register(final EventChannel<P> channel, final boolean localOnly, final EventsListener<P> eventsListener) {
		if (localOnly || remoteEventsPlugin.isEmpty()) {
			localEventsPlugin.register(channel, eventsListener);
		} else {
			//Can't assert if there is no remotePlugin, because listener component aren't aware of this (ex: PersistanceManager can't determine if there is a remoteEventPlugin)
			remoteEventsPlugin.get().register(channel, eventsListener);
		}
	}
}
