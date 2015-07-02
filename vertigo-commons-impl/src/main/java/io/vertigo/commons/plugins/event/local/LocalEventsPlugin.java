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
package io.vertigo.commons.plugins.event.local;

import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventChannel;
import io.vertigo.commons.event.EventListener;
import io.vertigo.commons.impl.event.EventPlugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author npiedeloup
 */
public final class LocalEventsPlugin implements EventPlugin {
	private final Map<EventChannel<?>, Set<EventListener<?>>> listenersPerChannel = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public <P extends Serializable> void emit(final EventChannel<P> channel, final Event<P> event) {
		final Set<EventListener<P>> eventsListeners = this.<P> obtainEventsListeners(channel);
		for (final EventListener<P> eventsListener : eventsListeners) {
			eventsListener.onEvent(event);
			//TODO gestion d'erreur ?
		}
	}

	/** {@inheritDoc} */
	@Override
	public <P extends Serializable> void register(final EventChannel<P> channel, final EventListener<P> eventsListener) {
		final Set<EventListener<P>> eventsListeners = this.<P> obtainEventsListeners(channel);
		eventsListeners.add(eventsListener);
	}

	private <P extends Serializable> Set<EventListener<P>> obtainEventsListeners(final EventChannel<P> channel) {
		Set listeners = listenersPerChannel.get(channel);
		if (listeners == null) {
			listeners = new HashSet<>();
			listenersPerChannel.put(channel, listeners);
		}
		return listeners;
	}
}
