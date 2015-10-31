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
package io.vertigo.core;

import io.vertigo.core.component.loader.ComponentLoader;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.definition.loader.DefinitionLoader;
import io.vertigo.core.param.ConfigManager;
import io.vertigo.core.spaces.component.ComponentSpace;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author pchretien
 */
public final class App implements AutoCloseable {
	private enum State {
		/** Composants en cours de démarrage*/
		starting,
		/** Composants configurés et démarrés*/
		active,
		/** Composants en cours d'arrét*/
		stopping,
		/** Terminé*/
		closed
	}

	private static Logger LOGGER = Logger.getLogger(App.class);

	//Start Date in milliseconds : used to have 'uptime'
	private final long start;
	private final AppConfig appConfig;
	private State state;

	private final Boot boot;
	private final DefinitionSpace definitionSpace;
	private final ComponentSpace componentSpace;

	//à remplacer par event ??
	private final List<AppListener> appListeners = new ArrayList<>();

	/**
	 * Constructor.
	 * @param appConfig Application configuration
	 */
	public App(final AppConfig appConfig) {
		Assertion.checkNotNull(appConfig);
		//-----
		start = System.currentTimeMillis();
		this.appConfig = appConfig;
		Home.setApp(this);
		state = State.starting;
		//-----
		try {
			//-----0. Boot (considered as a Module)
			boot = new Boot(appConfig.getBootConfig());
			//-----0. Boot
			componentSpace = new ComponentSpace();
			definitionSpace = new DefinitionSpace();

			//A faire créer par Boot : stratégie de chargement des composants à partir de ...
			final ComponentLoader componentLoader = new ComponentLoader(appConfig.getBootConfig().getAopEngine(), appConfig.getBootConfig().getElasticaEngine());
			//contient donc à minima resourceManager et configManager.
			componentLoader.injectBootComponents(componentSpace, appConfig.getBootConfig().getBootModuleConfig());

			//-----1. Load all definitions
			final DefinitionLoader definitionLoader = componentSpace.resolve(DefinitionLoader.class);
			definitionLoader.injectDefinitions(definitionSpace, appConfig.getModuleConfigs());

			//-----2. Load all components (and aspects).
			componentLoader.injectAllComponents(componentSpace, componentSpace.resolve(ConfigManager.class), appConfig.getModuleConfigs());
			//-----3. Print 
			if (!appConfig.getBootConfig().isSilence()) {
				appConfig.print(System.out);
			}
			//-----3. Start
			appStart();
			componentLoader.initializeAllComponents(componentSpace);
			appPostStart();
			//-----
			state = State.active;
		} catch (final Exception e) {
			close();
			throw new RuntimeException("an error occured when starting", e);
		}
	}

	/**
	 * @param appListener Listener of AppLifeCycle
	 */
	public void registerAppListener(final AppListener appListener) {
		Assertion.checkArgument(State.starting.equals(state), "Applisteners can't be registered at runtime");
		Assertion.checkNotNull(appListener);
		//-----
		appListeners.add(appListener);
	}

	private void appPostStart() {
		for (final AppListener appListener : appListeners) {
			appListener.onPostStart();
		}
	}

	private void appStart() {
		boot.start();
		definitionSpace.start();
		componentSpace.start();
		Thread.currentThread().setName("MAIN");
	}

	private void appStop() {
		componentSpace.stop();
		definitionSpace.stop();
		boot.stop();
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		//En cas d'erreur on essaie de fermer proprement les composants démarrés.
		Assertion.checkState(state == State.active || state == State.starting, "App with a state '{0}' can not be be closed", state);
		state = State.stopping;
		//-----
		try {
			appStop();
		} catch (final Exception e) {
			LOGGER.error("an error occured when stopping", e);
			//Quel que soit l'état, on part en échec de l'arrét.
			throw new RuntimeException("an error occured when stopping", e);
		} finally {
			state = State.closed;
			Home.setApp(null);
		}
	}

	/**
	 * @return Start Date in milliseconds
	 */
	public long getStartDate() {
		return start;
	}

	/**
	 * @return Application configuration
	 */
	public AppConfig getConfig() {
		return appConfig;
	}

	public DefinitionSpace getDefinitionSpace() {
		return definitionSpace;
	}

	public ComponentSpace getComponentSpace() {
		return componentSpace;
	}
}
