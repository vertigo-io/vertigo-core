/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
 * Publisher / Subscriber on event type for communication between components.
 * Listeners are configured to listen on the same JVM.
 *
 * The process is synchronous.
 * the suscribers execute their methods on the same thread.
 * The errors are not caught.
 * If one of the suscriber throws an error, this error is thrown on the post().
 *
 * The purpose of this pattern is to decouple the managers.
 * Managers that post don't need to know which components are listening.
 *
 * Example :
 *  - flushes local cache when an object is updated, deleted, inserted in the store
 *
 * WARNING :
 *  By default, EventBus is not distributed.
 *  A cache component should listen : a modification in one app should flush cache all over the system : it's cache component responsibility to do this
 *  An audit component should listen to do the audit log
 *
 * @author pchretien, npiedeloup
 */
public interface EventBusManager extends Manager {
	/**
	 * Posts an event.
	 * @param event Event
	 */
	void post(Event event);

	/**
	 * Registers a new listener for this type of Event.
	 * Registration must be executed during the init phase.
	 *
	 * @param eventType Type of event
	 * @param eventListener EventListener
	 */
	<E extends Event> void register(Class<E> eventType, EventListener<E> eventListener);

	/**
	 * Registers all methods annotated with @Suscriber on the object
	 * @param suscriberInstance
	 */
	void register(final Object suscriberInstance);

	/**
	 * Registers a dead event listener.
	 * @param eventListener EventListener
	 */
	void registerDead(final EventListener<Event> eventListener);
}
