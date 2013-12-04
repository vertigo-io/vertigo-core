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

import io.vertigo.kernel.component.ComponentSpace;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.di.configurator.ComponentSpaceImpl;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionSpace;

/**
 * Home : Classe d'entr�e sur toutes les modules. 
 * Cycle de vie : 
 *  on start : INACTIVE ==[starting]==> ACTIVE 
 *  on stop  : ACTIVE 	==[stopping]==>INACTIVE
 * 'starting' et 'stopping' sont des phases transitoires.
 * 
 * Si erreur durant la transition start (c'est � dire durant la phase starting) alors on proc�de � un arr�t via un stopping ==> INACTIVE.
 * Si erreur durant la transition stop (c'est � dire durant la phase stopping) alors on part sur une phase FAIL qui n�cessite un red�marrage. 
 *

 * @author pchretien
 */
public final class Home {
	//Start Date in milliseconds : used to have 'uptime'
	private static long start = -1;

	private static enum State {
		/** Composants non d�marr�s*/
		INACTIVE,
		/** Composants en cours de d�marrage*/
		starting,
		/** Composants configur�s et d�marr�s*/
		ACTIVE,
		/** Composants en cours d'arr�t*/
		stopping,
		/** Echec*/
		FAIL
	}

	private static final Home INSTANCE = new Home();

	private State state = State.INACTIVE;

	private final DefinitionSpace definitionSpace = new DefinitionSpace();
	private ComponentSpace componentSpace = ComponentSpaceImpl.EMPTY;

	private Home() {
		// Classe statique d'acc�s aux composants.
	}

	/**
	 * D�marrage de l'application.
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
			//	INSTANCE.jmx();
		} catch (final Throwable t) {
			//En cas d'erreur on essaie de fermer proprement les composants d�marr�s.
			INSTANCE.change(State.starting, State.stopping);
			// ---------------------------------------------------------------------
			INSTANCE.doStop();
			// ---------------------------------------------------------------------
			// L'arr�t s'est bien d�roul�.
			INSTANCE.change(State.stopping, State.INACTIVE);
			throw new VRuntimeException("Erreur lors de la phase de d�marrage", t);
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
			// L'arr�t s'est bien d�roul�.
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
	//-------------------M�thods publiques-------------------------------------
	//-------------------------------------------------------------------------
	public static DefinitionSpace getDefinitionSpace() {
		return INSTANCE.doGetDefinitionSpace();
	}

	public static ComponentSpace getComponentSpace() {
		return INSTANCE.doGetComponentSpace();
	}

	//-------------------------------------------------------------------------
	//-------------------M�thods priv�es---------------------------------------
	//-------------------------------------------------------------------------
	/**
	 * @return NameSpace 
	 */
	private DefinitionSpace doGetDefinitionSpace() {
		return definitionSpace;
	}

	/**
	 * Fermeture de l'application.
	 */
	private void doStop() {
		try {
			componentSpace.stop();
			definitionSpace.stop();
		} catch (final Throwable t) {
			//Quel que soit l'�tat, on part en �chec de l'arr�t.
			state = State.FAIL;
			throw new VRuntimeException("Erreur lors de l'arr�t", t);
		}
	}

	private ComponentSpace doGetComponentSpace() {
		//	check(State.ACTIVE, "'�tat non actif");
		//---------------------------------------------------------------------
		return componentSpace;
	}

	private void change(final State fromState, final State toState) {
		if (!state.equals(fromState)) {
			System.err.println("Container pas dans l'�tat attendu pour la transition ['" + fromState + "'==>'" + toState + "'], �tat actuel :'" + state + "' ");
		}
		//---------------------------------------------------------------------
		state = toState;
	}
}
