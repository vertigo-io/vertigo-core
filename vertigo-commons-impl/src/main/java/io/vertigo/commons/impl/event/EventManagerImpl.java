/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.commons.impl.event;

import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventBuilder;
import io.vertigo.commons.event.EventChannel;
import io.vertigo.commons.event.EventListener;
import io.vertigo.commons.event.EventManager;
import io.vertigo.commons.plugins.event.local.LocalEventsPlugin;

import java.io.Serializable;

/**
 * @author pchretien, npiedeloup
 */
public final class EventManagerImpl implements EventManager {
	private final EventPlugin localEventsPlugin = new LocalEventsPlugin();

	@Override
	public <P extends Serializable> void fire(final EventChannel<P> channel, final P payload) {
		final Event<P> event = new EventBuilder().withPayload(payload).build();
		localEventsPlugin.emit(channel, event);
	}

	/**
	 * Register a new listener for this channel.
	 * @param channel ChannelName to listen
	 * @param eventsListener EventsListener
	 */
	@Override
	public <P extends Serializable> void register(final EventChannel<P> channel, final EventListener<P> eventsListener) {
		localEventsPlugin.register(channel, eventsListener);
	}
}
