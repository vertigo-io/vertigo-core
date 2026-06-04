/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.component.loader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.component.ComponentSpace;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.util.StringUtil;

/**
 * Centralized access to components and plugins.
 *
 * Components and their initializers are instantiated by injection of:
 *  - parameters declared at the component scope,
 *  - other components.
 *
 * Plugins are instantiated by injection of:
 *  - parameters declared at the plugin scope,
 *  - other components.
 *
 * A plugin cannot be injected into another plugin; it can only be injected into THE component it is intended for.
 * Components (avoiding cyclic dependencies) can be injected into components, plugins and initializers.
 *
 * @author pchretien
 */
public final class ComponentSpaceWritable implements ComponentSpace, Activeable {

	private static final Logger LOGGER = LogManager.getLogger(ComponentSpaceWritable.class);
	/**
	 * Components (sorted by creation)
	 */
	private final Map<String, CoreComponent> components = new LinkedHashMap<>();
	/**
	 * Started components are sublist of components.values(). They are added after the start call of a component.
	 */
	private final List<CoreComponent> startedComponents = new ArrayList<>();
	private final AtomicBoolean locked = new AtomicBoolean(false);

	public ComponentSpaceWritable() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		startComponents();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		stopComponents();
		clear();
	}

	/**
	 * Register a component with its id.
	 * @param componentId id of the component
	 * @param component instance of the component
	 */
	void registerComponent(final String componentId, final CoreComponent component) {
		Assertion.check()
				.isFalse(locked.get(), "Registration is now closed. A component can be registered only during the boot phase")
				.isNotBlank(componentId)
				.isNotNull(component);
		//-----
		final Object previous = components.put(componentId, component);
		Assertion.check()
				.isNull(previous, "component '{0}' already registered", componentId);
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		Assertion.check().isNotBlank(id);
		//-----
		final String normalizedId = StringUtil.first2LowerCase(id);
		return components.containsKey(normalizedId);
	}

	/** {@inheritDoc} */
	@Override
	public <C> C resolve(final String id, final Class<C> componentClass) {
		Assertion.check()
				.isNotBlank(id)
				.isNotNull(componentClass);
		//-----
		final String normalizedId = StringUtil.first2LowerCase(id);
		Assertion.check().isTrue(contains(normalizedId), "No component registered for id = {0} among {1}", normalizedId, keySet());
		//-----
		return componentClass.cast(components.get(normalizedId));
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return components.keySet();
	}

	private static void startComponent(final CoreComponent component) {
		if (component instanceof Activeable activeable) {
			activeable.start();
		}
	}

	private static void stopComponent(final CoreComponent component) {
		if (component instanceof Activeable activeable) {
			activeable.stop();
		}
	}

	private void clear() {
		components.clear();
	}

	private void startComponents() {
		for (final CoreComponent component : components.values()) {
			startedComponents.add(component);
			startComponent(component);
		}
	}

	private void stopComponents() {
		/* Stop all managers.*/
		//Stop in reverse order of registration.
		//Only stop components that were actually started.
		final List<CoreComponent> reversedComponents = new ArrayList<>(startedComponents);
		java.util.Collections.reverse(reversedComponents);

		for (final CoreComponent component : reversedComponents) {
			try {
				stopComponent(component);
			} catch (final Exception e) {
				LOGGER.error("Failed stopping component " + component, e);
			}
		}
	}

	/**
	 * Close registration of components.
	 * After calling this method no more components are added to the componentSpace.
	 */
	void closeRegistration() {
		//registration is now closed.
		locked.set(true);
	}
}
