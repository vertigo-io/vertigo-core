package io.vertigo.dynamo.impl.events;

import io.vertigo.dynamo.events.Event;
import io.vertigo.dynamo.events.EventBuilder;
import io.vertigo.dynamo.events.EventChannel;
import io.vertigo.dynamo.events.EventsListener;
import io.vertigo.dynamo.events.EventsManager;
import io.vertigo.dynamo.plugins.events.local.LocalEventsPlugin;
import io.vertigo.lang.Assertion;

import java.io.Serializable;

import javax.inject.Inject;

/**
 * @author pchretien, npiedeloup
 */
public final class EventsManagerImpl implements EventsManager {
	private final EventsPlugin localEventsPlugin = new LocalEventsPlugin();
	private final EventsPlugin remoteEventsPlugin;

	@Inject
	public EventsManagerImpl(final EventsPlugin remoteEventsPlugin) {
		Assertion.checkNotNull(remoteEventsPlugin);
		//-----
		this.remoteEventsPlugin = remoteEventsPlugin;
	}

	@Override
	public <P extends Serializable> void fire(final EventChannel<P> channel, final P payload) {
		final Event<P> event = new EventBuilder().withPayload(payload).build();
		localEventsPlugin.emit(channel, event);
		remoteEventsPlugin.emit(channel, event);
	}

	/**
	 * Register a new listener for this channel.
	 * @param channel ChannelName to listen
	 * @param localOnly If this listener is local sent event only
	 * @param eventsListener EventsListener
	 */
	@Override
	public <P extends Serializable> void register(final EventChannel<P> channel, final boolean localOnly, final EventsListener<P> eventsListener) {
		if (localOnly) {
			localEventsPlugin.register(channel, eventsListener);
		} else {
			remoteEventsPlugin.register(channel, eventsListener);
		}
	}
}
