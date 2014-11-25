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
import io.vertigo.core.config.ModuleConfig;
import io.vertigo.core.spaces.component.ComponentSpace;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
	//Start Date in milliseconds : used to have 'uptime'
	private static long start = -1;
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

	private State state = State.INACTIVE;

	private final DefinitionSpace definitionSpace = new DefinitionSpace();
	private ComponentSpace componentSpace = ComponentSpace.EMPTY;
	private AppConfig currentAppConfig;

	private Home() {
		// Classe statique d'accès aux composants.
	}

	//-------------------------------------------------------------------------
	//-------------------Méthods publiques-------------------------------------
	//-------------------------------------------------------------------------
	/**
	 * Démarrage de l'application.
	 * @param appConfig AppConfig
	 */
	public static void start(final AppConfig appConfig) {
		INSTANCE.doStart(appConfig);
	}

	/**
	 * @return Current AppConfig
	 */
	public static AppConfig getAppConfig() {
		return INSTANCE.currentAppConfig;
	}

	/**
	 * Fermeture de l'application.
	 */
	public static void stop() {
		//Une instance inactive peut être stopé
		if (INSTANCE.state != State.INACTIVE) {
			INSTANCE.doStop(State.ACTIVE);
		}
	}

	/**
	 * @return Start Date in milliseconds
	 */
	public static long getStartDate() {
		return start;
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

	//-------------------------------------------------------------------------
	//-------------------Méthods privées---------------------------------------
	//-------------------------------------------------------------------------
	private void doStart(final AppConfig appConfig) {
		Assertion.checkNotNull(appConfig);
		//-------------------------------------------------------------------------
		change(State.INACTIVE, State.starting);
		currentAppConfig = appConfig;
		try {
			Assertion.checkState(definitionSpace.isEmpty(), "DefinitionSpace must be empty");
			//---
			initLog(appConfig.getParams());
			//---
			componentSpace = new ComponentSpace(appConfig);
			//----
			for (final ModuleConfig moduleConfig : appConfig.getModuleConfigs()) {
				definitionSpace.injectResources(moduleConfig);
				componentSpace.injectComponents(moduleConfig);
			}
			//--
			componentSpace.start();
			//	INSTANCE.jmx();
		} catch (final Throwable t) {
			doStop(State.starting);
			throw new RuntimeException("an error occured when starting", t);
		}
		change(State.starting, State.ACTIVE);
		//---
		start = System.currentTimeMillis();
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
		} catch (final Throwable t) {
			//Quel que soit l'état, on part en échec de l'arrét.
			state = State.FAIL;
			throw new RuntimeException("an error occured when stopping", t);
		}
		// L'arrét s'est bien déroulé.
		INSTANCE.change(State.stopping, State.INACTIVE);
	}

	private ComponentSpace doGetComponentSpace() {
		//	check(State.ACTIVE, "'état non actif");
		//---------------------------------------------------------------------
		return componentSpace;
	}

	private void change(final State fromState, final State toState) {
		if (!state.equals(fromState)) {
			System.err.println("Container pas dans l'état attendu pour la transition ['" + fromState + "'==>'" + toState + "'], état actuel :'" + state + "' ");
		}
		//---------------------------------------------------------------------
		state = toState;
	}

	private static void initLog(final Map<String, String> params) {
		final String log4jFileName = params.get("log4j.configurationFileName");
		if (log4jFileName != null) {
			final boolean log4jFormatXml = log4jFileName.endsWith(".xml");
			final URL url = Home.class.getResource(log4jFileName);
			if (url != null) {
				if (log4jFormatXml) {
					DOMConfigurator.configure(url);
				} else {
					PropertyConfigurator.configure(url);
				}
				Logger.getRootLogger().info("Log4J configuration chargée (resource) : " + url.getFile());
			} else {
				Assertion.checkArgument(new File(log4jFileName).exists(), "Fichier de configuration log4j : {0} est introuvable", log4jFileName);
				// Avec configureAndWatch (utilise un anonymous thread)
				// on peut modifier à chaud le fichier de conf log4j
				// mais en cas de hot-deploy, le thread reste présent ce qui peut-entrainer des problèmes.
				if (log4jFormatXml) {
					DOMConfigurator.configureAndWatch(log4jFileName);
				} else {
					PropertyConfigurator.configureAndWatch(log4jFileName);
				}
			}
			Logger.getRootLogger().info("Log4J configuration chargée (fichier) : " + log4jFileName);
		}
	}
}
