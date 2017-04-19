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
package io.vertigo.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.ComponentInitializerConfig;
import io.vertigo.core.component.ComponentInitializer;
import io.vertigo.core.component.ComponentSpace;
import io.vertigo.core.component.ComponentSpaceWritable;
import io.vertigo.core.component.di.injector.DIInjector;
import io.vertigo.core.component.loader.ComponentLoader;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.DefinitionSpaceWritable;
import io.vertigo.core.definition.loader.DefinitionLoader;
import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * The app class is the core of vertigo.
 * @author pchretien
 */
public final class AutoCloseableApp implements App, AutoCloseable {
	private enum State {
		/** Components are starting*/
		STARTING,
		/** Components are started*/
		ACTIVE,
		/** Components are stopping*/
		STOPPING,
		/** App is closed, good bye !*/
		CLOSED
	}

	private static final Logger LOGGER = Logger.getLogger(AutoCloseableApp.class);

	//Start : used to have 'uptime'
	private final Instant start;
	private final AppConfig appConfig;
	private State state;

	private final DefinitionSpaceWritable definitionSpaceWritable;
	private final ComponentSpaceWritable componentSpaceWritable;

	//à remplacer par event ??
	private final List<Runnable> postStartFunctions = new ArrayList<>();

	/**
	 * Constructor.
	 * @param appConfig Application configuration
	 */
	public AutoCloseableApp(final AppConfig appConfig) {
		Assertion.checkNotNull(appConfig);
		//-----
		start = Instant.now();
		this.appConfig = appConfig;
		Home.setApp(this);
		state = State.STARTING;
		//-----
		//-----0. Boot (considered as a Module)
		final Boot boot = new Boot(appConfig.getBootConfig());
		boot.init();
		//-----0. Boot
		componentSpaceWritable = new ComponentSpaceWritable();
		definitionSpaceWritable = new DefinitionSpaceWritable();

		try {
			//A faire créer par Boot : stratégie de chargement des composants à partir de ...
			final ComponentLoader componentLoader = new ComponentLoader(componentSpaceWritable, appConfig.getBootConfig().getAopPlugin());
			//contient donc à minima resourceManager et paramManager.

			//Dans le cas de boot il n,'y a ni initializer, ni aspects, ni definitions
			componentLoader.injectComponents(Optional.empty(), "boot",
					appConfig.getBootConfig().getComponentConfigs());

			//-----1. Load all definitions
			final DefinitionLoader definitionLoader = new DefinitionLoader(definitionSpaceWritable, componentSpaceWritable);
			definitionLoader.createDefinitions(appConfig.getModuleConfigs())
					.forEach(definitionSpaceWritable::registerDefinition);
			//Here all definitions are registered into the definitionSpace

			//-----2. Load all components (and aspects).
			componentLoader.injectAllComponentsAndAspects(Optional.of(componentSpaceWritable.resolve(ParamManager.class)), appConfig.getModuleConfigs());
			//-----3. Print
			if (appConfig.getBootConfig().isVerbose()) {
				Logo.printCredits(System.out);
				appConfig.print(System.out);
			}
			//-----4. post-Initialize all components
			initializeAllComponents();
			//-----5. Start
			appStart();
			//-----
			state = State.ACTIVE;
			//-----6. AfterStart (application is active)
			appPostStart();
		} catch (final Exception e) {
			close();
			throw new IllegalStateException("an error occured when starting", e);
		}
	}

	@Override
	public void registerPostStartFunction(final Runnable postStartFunction) {
		Assertion.checkArgument(State.STARTING.equals(state), "Applisteners can't be registered at runtime");
		Assertion.checkNotNull(postStartFunction);
		//-----
		postStartFunctions.add(postStartFunction);
	}

	private void appPostStart() {
		for (final Runnable postStartFunction : postStartFunctions) {
			postStartFunction.run();
		}
	}

	private void appStart() {
		definitionSpaceWritable.start();
		componentSpaceWritable.start();
		Thread.currentThread().setName("MAIN");
	}

	private void appStop() {
		componentSpaceWritable.stop();
		definitionSpaceWritable.stop();
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		//En cas d'erreur on essaie de fermer proprement les composants démarrés.
		Assertion.checkState(state == State.ACTIVE || state == State.STARTING, "App with a state '{0}' can not be be closed", state);
		state = State.STOPPING;
		//-----
		try {
			appStop();
		} catch (final Exception e) {
			LOGGER.error("an error occured when stopping", e);
			//Quel que soit l'état, on part en échec de l'arrét.
			throw WrappedException.wrap(e, "an error occured when stopping");
		} finally {
			state = State.CLOSED;
			Home.setApp(null);
		}
	}

	@Override
	public Instant getStart() {
		return start;
	}

	@Override
	public AppConfig getConfig() {
		return appConfig;
	}

	@Override
	public DefinitionSpace getDefinitionSpace() {
		//We publish the Read Only version
		return definitionSpaceWritable;
	}

	@Override
	public ComponentSpace getComponentSpace() {
		//We publish the Read Only version
		return componentSpaceWritable;
	}

	private void initializeAllComponents() {
		for (final ComponentInitializerConfig componentInitializerConfig : appConfig.getComponentInitializerConfigs()) {
			Assertion.checkArgument(!Activeable.class.isAssignableFrom(componentInitializerConfig.getInitializerClass()),
					"The initializer '{0}' can't be activeable", componentInitializerConfig.getInitializerClass());
			final ComponentInitializer componentInitializer = DIInjector.newInstance(componentInitializerConfig.getInitializerClass(), componentSpaceWritable);
			componentInitializer.init();
		}
	}
}
