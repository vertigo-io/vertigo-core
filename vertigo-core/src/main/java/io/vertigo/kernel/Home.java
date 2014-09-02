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
package io.vertigo.kernel;

import io.vertigo.core.component.ComponentSpace;
import io.vertigo.core.di.configurator.ComponentSpaceConfig;
import io.vertigo.core.di.configurator.ComponentSpaceImpl;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionSpace;
import io.vertigo.kernel.resource.ResourceSpace;

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

	private static final Home INSTANCE = new Home();

	private State state = State.INACTIVE;

	private final DefinitionSpace definitionSpace = new DefinitionSpace();
	private ComponentSpace componentSpace = ComponentSpaceImpl.EMPTY;
	private final ResourceSpace resourceSpace = new ResourceSpace();

	private Home() {
		// Classe statique d'accès aux composants.
	}

	/**
	 * Démarrage de l'application.
	 * @param componentSpaceConfig ComponentSpaceConfig
	 */
	public static void start(final ComponentSpaceConfig componentSpaceConfig) {
		Assertion.checkNotNull(componentSpaceConfig);
		//-------------------------------------------------------------------------
		INSTANCE.change(State.INACTIVE, State.starting);
		try {
			INSTANCE.definitionSpace.start();
			//---
			INSTANCE.componentSpace = new ComponentSpaceImpl(componentSpaceConfig);
			INSTANCE.componentSpace.start();
			INSTANCE.resourceSpace.start();
			//	INSTANCE.jmx();
		} catch (final Throwable t) {
			//En cas d'erreur on essaie de fermer proprement les composants démarrés.
			INSTANCE.change(State.starting, State.stopping);
			// ---------------------------------------------------------------------
			INSTANCE.doStop();
			// ---------------------------------------------------------------------
			// L'arrét s'est bien déroulé.
			INSTANCE.change(State.stopping, State.INACTIVE);
			throw new RuntimeException("an error occured when starting", t);
		}
		INSTANCE.change(State.starting, State.ACTIVE);
		//---
		start = System.currentTimeMillis();
	}

	/**
	 * Fermeture de l'application.
	 */
	public static void stop() {
		//il est toujours possible de re-stopper. 
		if (INSTANCE.state != State.INACTIVE) {
			INSTANCE.change(State.ACTIVE, State.stopping);
			INSTANCE.doStop();
			// L'arrét s'est bien déroulé.
			INSTANCE.change(State.stopping, State.INACTIVE);
		}
	}

	/**
	 * @return Start Date in milliseconds
	 */
	public static long getStartDate() {
		return start;
	}

	//-------------------------------------------------------------------------
	//-------------------Méthods publiques-------------------------------------
	//-------------------------------------------------------------------------
	/**
	 * @return ResourceSpace contains application's Resources
	 */
	public static ResourceSpace getResourceSpace() {
		return INSTANCE.doGetResourceSpace();
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

	private ResourceSpace doGetResourceSpace() {
		return resourceSpace;
	}

	private DefinitionSpace doGetDefinitionSpace() {
		return definitionSpace;
	}

	/**
	 * Fermeture de l'application.
	 */
	private void doStop() {
		try {
			resourceSpace.stop();
			componentSpace.stop();
			definitionSpace.stop();
		} catch (final Throwable t) {
			//Quel que soit l'état, on part en échec de l'arrét.
			state = State.FAIL;
			throw new RuntimeException("an error occured when stopping", t);
		}
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
}
