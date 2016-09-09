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
package io.vertigo.commons.impl.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.vertigo.commons.eventbus.Event;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.eventbus.EventListener;
import io.vertigo.commons.eventbus.EventSuscriber;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * @author pchretien, npiedeloup
 */
public final class EventBusManagerImpl implements EventBusManager {
	private final List<EventBusSubscription> subscriptions = new ArrayList<>();
	private final List<EventListener> deadEventListeners = new ArrayList<>();

	/** {@inheritDoc} */
	@Override
	public void post(final Event event) {
		Assertion.checkNotNull(event);
		//-----
		boolean emitted = false;
		for (final EventBusSubscription subscription : subscriptions) {
			if (subscription.accept(event)) {
				subscription.getListener().onEvent(event);
				emitted = true;
			}
		}

		//manages dead event
		if (!emitted) {
			for (final EventListener deadEventlistener : deadEventListeners) {
				deadEventlistener.onEvent(event);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void register(final Object suscriberInstance) {
		Assertion.checkNotNull(suscriberInstance);
		//-----
		int count = 0;
		//1. search all methods
		for (final Method method : ClassUtil.getAllMethods(suscriberInstance.getClass(), EventSuscriber.class)) {
			Assertion.checkArgument(void.class.equals(method.getReturnType()), "suscriber's methods  of class {0} must be void instead of {1}", suscriberInstance.getClass(), method.getReturnType());
			Assertion.checkArgument(method.getName().startsWith("on"), "suscriber's methods of class {0} must start with on", suscriberInstance.getClass());
			Assertion.checkArgument(method.getParameterTypes().length == 1, "suscriber's methods of class {0} must be void onXXX(Event e)", suscriberInstance.getClass());
			Assertion.checkArgument(Event.class.isAssignableFrom(method.getParameterTypes()[0]), "suscriber's methods of class {0} must be 'void onXXX(E extends Event)'", suscriberInstance.getClass());
			//-----
			//2. For each method register a listener
			count++;
			method.setAccessible(true);
			register((Class<? extends Event>) method.getParameterTypes()[0],
					(event) -> ClassUtil.invoke(suscriberInstance, method, event));
		}
		//3. Check that there is almost one suscriber on this object.
		Assertion.checkState(count > 0, "no suscriber found on class {0}", suscriberInstance.getClass());
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Event> void register(final Class<E> eventType, final EventListener<E> eventListener) {
		Assertion.checkNotNull(eventType);
		Assertion.checkNotNull(eventListener);
		//-----
		subscriptions.add(new EventBusSubscription<>(eventType, eventListener));
	}

	/** {@inheritDoc} */
	@Override
	public void registerDead(final EventListener<Event> eventListener) {
		Assertion.checkNotNull(eventListener);
		//-----
		deadEventListeners.add(eventListener);
	}
}
