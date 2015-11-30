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
package io.vertigo.commons.eventbus;

import io.vertigo.lang.Manager;

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
public interface EventBusManager extends Manager {
	/**
	 * post an event.
	 * @param event Event 
	 */
	void post(Event event);

	/**
	 * Register a new listener for this type of Event.
	 * @param type Type of event
	 * @param eventListener EventListener
	 */
	//	@Init
	<E extends Event> void register(Class<E> eventType, EventListener<E> eventListener);

	/**
	 * Register all methods annotaed with @Suscriber on the object
	 * @param suscriberInstance
	 */
	void register(final Object suscriberInstance);

	/**
	 * Register a dead event listener.
	 * @param eventListener EventListener
	 */
	void registerDead(final EventListener<Event> eventListener);
}
