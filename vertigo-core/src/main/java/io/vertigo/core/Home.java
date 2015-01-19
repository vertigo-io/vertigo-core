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
 * Life Cycle
 * starting ==> active ==> stopping ==> closed
 *
 * When error during starting
 * starting ==> stopping ==> closed ()
 *
 * 'starting' and 'stopping' sont are ephemeral transitions.
 *
 * @author pchretien
 */
public final class Home {

	public static final class App implements AutoCloseable {
		private static enum State {
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

		private final DefinitionSpace definitionSpace;
		private final ComponentSpace componentSpace;

		public App(final AppConfig appConfig) {
			Assertion.checkNotNull(appConfig);
			//-----
			start = System.currentTimeMillis();
			this.appConfig = appConfig;
			Home.CURRENT_APP = this;
			state = State.starting;
			//-----
			try {
				if (appConfig.getLogConfig().isDefined()) {
					initLog(appConfig.getLogConfig().get());
				}
				//-----
				definitionSpace = new DefinitionSpace();
				componentSpace = new ComponentSpace(appConfig);
				//----
				for (final ModuleConfig moduleConfig : appConfig.getModuleConfigs()) {
					definitionSpace.injectResources(moduleConfig);
					componentSpace.injectComponents(moduleConfig);
					componentSpace.injectAspects(moduleConfig);
				}
				//-----
				componentSpace.start();
				//	INSTANCE.jmx();
				this.state = State.active;
			} catch (final Throwable t) {
				this.close();
				throw new RuntimeException("an error occured when starting", t);
			}
		}

		@Override
		public void close() {
			//En cas d'erreur on essaie de fermer proprement les composants démarrés.
			Assertion.checkState(state == State.active || state == State.starting, "App with a state '{0}' can not be be closed", state);
			state = State.stopping;
			//-----
			try {
				definitionSpace.clear();
				componentSpace.stop();
			} catch (final Throwable t) {
				LOGGER.error("an error occured when stopping", t);
				//Quel que soit l'état, on part en échec de l'arrét.
				throw new RuntimeException("an error occured when stopping", t);
			} finally {
				state = State.closed;
				CURRENT_APP = null;
			}
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

		private DefinitionSpace getDefinitionSpace() {
			return definitionSpace;
		}

		private ComponentSpace getComponentSpace() {
			return componentSpace;
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

	private static App CURRENT_APP = null;

	private Home() {
		// Classe statique d'accès aux composants.
	}

	public static App getApp() {
		Assertion.checkNotNull(CURRENT_APP, "app has not been started");
		return CURRENT_APP;
	}

	/**
	 * @return DefinitionSpace contains application's Definitions
	 */
	public static DefinitionSpace getDefinitionSpace() {
		return getApp().getDefinitionSpace();
	}

	/**
	 * @return ComponentSpace contains application's Components
	 */
	public static ComponentSpace getComponentSpace() {
		return getApp().getComponentSpace();
	}

}
