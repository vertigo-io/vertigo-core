/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
import java.util.function.Consumer;

import io.vertigo.app.Home;
import io.vertigo.commons.eventbus.Event;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.eventbus.EventSuscriber;
import io.vertigo.core.component.ComponentSpace;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.util.ClassUtil;

/**
 * @author pchretien, npiedeloup
 */
public final class EventBusManagerImpl implements EventBusManager, Activeable {
	private final List<EventBusSubscription> subscriptions = new ArrayList<>();
	private final List<Consumer<Event>> deadEventListeners = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public EventBusManagerImpl() {
		Home.getApp().registerPreActivateFunction(this::registerAllSubscribers);
	}

	@Override
	public void start() {
		// nothing
	}

	@Override
	public void stop() {
		subscriptions.clear();
		deadEventListeners.clear();
	}

	private void registerAllSubscribers() {
		final ComponentSpace componentSpace = Home.getApp().getComponentSpace();
		for (final String componentId : componentSpace.keySet()) {
			registerSubscribers(componentSpace.resolve(componentId, Component.class));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void post(final Event event) {
		Assertion.checkNotNull(event);
		//-----
		final long emitted = subscriptions.stream()
				.filter(subscription -> subscription.match(event))
				.peek(subscription -> subscription.getListener().accept(event))
				.count();

		//manages dead event
		if (emitted == 0) {
			deadEventListeners
					.forEach(deadEventlistener -> deadEventlistener.accept(event));
		}
	}

	/**
	 * Registers all methods annotated with @Suscriber on the object
	 * @param suscriberInstance
	 */
	private void registerSubscribers(final Component suscriberInstance) {
		Assertion.checkNotNull(suscriberInstance);
		//-----
		//1. search all methods
		for (final Method method : ClassUtil.getAllMethods(suscriberInstance.getClass(), EventSuscriber.class)) {
			Assertion.checkArgument(void.class.equals(method.getReturnType()), "suscriber's methods  of class {0} must be void instead of {1}", suscriberInstance.getClass(), method.getReturnType());
			Assertion.checkArgument(method.getName().startsWith("on"), "suscriber's methods of class {0} must start with on", suscriberInstance.getClass());
			Assertion.checkArgument(method.getParameterTypes().length == 1, "suscriber's methods of class {0} must be void onXXX(Event e)", suscriberInstance.getClass());
			Assertion.checkArgument(Event.class.isAssignableFrom(method.getParameterTypes()[0]), "suscriber's methods of class {0} must be 'void onXXX(E extends Event)'", suscriberInstance.getClass());
			//-----
			//2. For each method register a listener
			final Class<? extends Event> eventType = (Class<? extends Event>) method.getParameterTypes()[0];
			subscribe(eventType,
					event -> ClassUtil.invoke(suscriberInstance, method, event));
		}
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Event> void subscribe(final Class<E> eventType, final Consumer<E> eventConsumer) {
		Assertion.checkNotNull(eventType);
		Assertion.checkNotNull(eventConsumer);
		//-----
		subscriptions.add(new EventBusSubscription<>(eventType, eventConsumer));
	}

	/** {@inheritDoc} */
	@Override
	public void registerDead(final Consumer<Event> eventConsumer) {
		Assertion.checkNotNull(eventConsumer);
		//-----
		deadEventListeners.add(eventConsumer);
	}
}
