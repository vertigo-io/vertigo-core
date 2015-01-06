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

import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.LogConfig;
import io.vertigo.core.config.ModuleConfig;
import io.vertigo.core.spaces.component.ComponentSpace;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Home : Classe d'entrée sur toutes les modules.
 * Cycle de vie :
 *  on start : INACTIVE ==[starting]==> ACTIVE
 *  on stop  : ACTIVE 	==[stopping]==>INACTIVE
 * 'starting' et 'stopping' sont des phases transitoires.
 *
 * Si erreur durant la transition start (c'est à dire durant la phase starting) alors on procéde à un arrét via un stopping ==> INACTIVE.
 * Si erreur durant la transition stop (c'est à dire durant la phase stopping) alors on part sur une phase FAIL qui nécessite un redémarrage.
 *

 * @author pchretien
 */
public final class Home {
	public static final class App implements AutoCloseable {
		//Start Date in milliseconds : used to have 'uptime'
		private final long start = System.currentTimeMillis();
		private final AppConfig appConfig;

		public App(final AppConfig appConfig) {
			Assertion.checkNotNull(appConfig);
			//-----
			this.appConfig = appConfig;
			Home.INSTANCE.start(appConfig);
		}

		@Override
		public void close() {
			Home.INSTANCE.stop();
		}

		/**
		 * @return Start Date in milliseconds
		 */
		public long getStartDate() {
			return start;
		}

		public AppConfig getConfig() {
			return appConfig;
		}

	}

	private static final Home INSTANCE = new Home();

	private static enum State {
		/** Composants non démarrés*/
		INACTIVE,
		/** Composants en cours de démarrage*/
		starting,
		/** Composants configurés et démarrés*/
		ACTIVE,
		/** Composants en cours d'arrét*/
		stopping,
		/** Echec*/
		FAIL
	}

	private App app = null;
	private State state = State.INACTIVE;

	private final DefinitionSpace definitionSpace = new DefinitionSpace();
	private ComponentSpace componentSpace = ComponentSpace.EMPTY;

	private Home() {
		// Classe statique d'accès aux composants.
	}

	public static App getApp() {
		Assertion.checkNotNull(INSTANCE.app, "app has not been started");
		return INSTANCE.app;
	}

	/**
	 * Fermeture de l'application.
	 */
	private void stop() {
		//Une instance inactive peut être stopé
		if (state != State.INACTIVE) {
			doStop(State.ACTIVE);
		}
	}

	/**
	 * @return DefinitionSpace contains application's Definitions
	 */
	public static DefinitionSpace getDefinitionSpace() {
		return INSTANCE.doGetDefinitionSpace();
	}

	/**
	 * @return ComponentSpace contains application's Components
	 */
	public static ComponentSpace getComponentSpace() {
		return INSTANCE.doGetComponentSpace();
	}

	/**
	 * Démarrage de l'application.
	 * @param appConfig AppConfig
	 */
	private void start(final AppConfig appConfig) {
		Assertion.checkNotNull(appConfig);
		//-----
		change(State.INACTIVE, State.starting);
		try {
			Assertion.checkState(definitionSpace.isEmpty(), "DefinitionSpace must be empty");
			//---
			if (appConfig.getLogConfig().isDefined()) {
				initLog(appConfig.getLogConfig().get());
			}
			//---
			componentSpace = new ComponentSpace(appConfig);
			//----
			for (final ModuleConfig moduleConfig : appConfig.getModuleConfigs()) {
				definitionSpace.injectResources(moduleConfig);
				componentSpace.injectComponents(moduleConfig);
				componentSpace.injectAspects(moduleConfig);
			}
			//--
			componentSpace.start();
			//	INSTANCE.jmx();
		} catch (final Throwable t) {
			doStop(State.starting);
			throw new RuntimeException("an error occured when starting", t);
		}
		change(State.starting, State.ACTIVE);
	}

	private DefinitionSpace doGetDefinitionSpace() {
		return definitionSpace;
	}

	/**
	 * Fermeture de l'application.
	 */
	private void doStop(final State from) {
		//En cas d'erreur on essaie de fermer proprement les composants démarrés.
		change(from, State.stopping);
		//-----
		try {
			definitionSpace.clear();
			componentSpace.stop();
			// L'arrét s'est bien déroulé.
			change(State.stopping, State.INACTIVE);
		} catch (final Throwable t) {
			//Quel que soit l'état, on part en échec de l'arrét.
			state = State.FAIL;
			throw new RuntimeException("an error occured when stopping", t);
		} finally {
			app = null;
		}
	}

	private ComponentSpace doGetComponentSpace() {
		//	check(State.ACTIVE, "'état non actif");
		//-----
		return componentSpace;
	}

	private void change(final State fromState, final State toState) {
		if (!state.equals(fromState)) {
			System.err.println("Container pas dans l'état attendu pour la transition ['" + fromState + "'==>'" + toState + "'], état actuel :'" + state + "' ");
		}
		//-----
		state = toState;
	}

	private static void initLog(final LogConfig log4Config) {
		Assertion.checkNotNull(log4Config);
		//-----
		final String log4jFileName = log4Config.getFileName();
		Assertion.checkArgument(log4jFileName.endsWith(".xml"), "Use the XML format for log4j configurations (instead of : {0}).", log4jFileName);
		final URL url = Home.class.getResource(log4jFileName);
		if (url != null) {
			DOMConfigurator.configure(url);
			Logger.getRootLogger().info("Log4J configuration chargée (resource) : " + url.getFile());
		} else {
			Assertion.checkArgument(new File(log4jFileName).exists(), "Fichier de configuration log4j : {0} est introuvable", log4jFileName);
			// Avec configureAndWatch (utilise un anonymous thread)
			// on peut modifier à chaud le fichier de conf log4j
			// mais en cas de hot-deploy, le thread reste présent ce qui peut-entrainer des problèmes.
			DOMConfigurator.configureAndWatch(log4jFileName);
		}
		Logger.getRootLogger().info("Log4J configuration chargée (fichier) : " + log4jFileName);
	}
}
