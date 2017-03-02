/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package io.vertigo.commons.plugins.analytics.analytica;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import io.vertigo.commons.plugins.analytics.analytica.process.AProcess;
import io.vertigo.commons.plugins.analytics.analytica.process.AProcessBuilder;
import io.vertigo.lang.Assertion;

/**
 * Datas collector agent.
 * Collect and build Process.
 * Building process are bind to current thread.
 *
 * @author pchretien, npiedeloup
 * @version $Id: AgentManagerImpl.java,v 1.7 2012/03/29 08:48:19 npiedeloup Exp $
 */
final class AProcessCollector {
	private final String appName;
	private final String location;

	/**
	 * Processus binde sur le thread courant. Le processus , recoit les notifications des sondes placees dans le code de
	 * l'application pendant le traitement d'une requete (thread).
	 */
	private static final ThreadLocal<Deque<AProcessBuilder>> THREAD_LOCAL_PROCESS = new ThreadLocal<>();

	/**
	 * Constructor.
	 * Should be created only once.
	 * @param processConnector Collector output connector
	 */

	AProcessCollector(final String appName, final String location) {
		Assertion.checkNotNull(appName, "appName is required");
		Assertion.checkNotNull(location, "location is required");
		//-----------------------------------------------------------------
		this.location = location;
		this.appName = appName;
	}

	/**
	 * Retourne le premier element de la pile (sans le retirer).
	 * @return Premier element de la pile
	 */
	private static AProcessBuilder peek() {
		return getStack().peek();
	}

	/**
	 * Retire le premier élément de la pile.
	 * @return Premier élément de la pile
	 */
	private static AProcessBuilder pop() {
		return getStack().pop();
	}

	private static Deque<AProcessBuilder> getStack() {
		final Deque<AProcessBuilder> stack = THREAD_LOCAL_PROCESS.get();
		Assertion.checkNotNull(stack, "Pile non initialisée : startProcess()");
		return stack;
	}

	private static void push(final AProcessBuilder processBuilder) {
		Deque<AProcessBuilder> stack = THREAD_LOCAL_PROCESS.get();
		if (stack == null) {
			stack = new LinkedList<>();
			THREAD_LOCAL_PROCESS.set(stack);
		}
		//---------------------------------------------------------------------
		if (stack.size() >= 100) {
			throw new IllegalStateException("La pile des AProcess atteind une profondeur de 100, il est probable qu'une fermeture de AProcess ait été oubliée.\nStack:" + stack);
		}
		//---------------------------------------------------------------------
		stack.push(processBuilder);
	}

	/**
	 * Enregistre dans le thread courant le demarrage d'un process.
	 * Doit respecter les regles sur le nom d'un process.
	 * @param type Type de process
	 * @param category Process category
	 * @return this analytics process collector
	 */
	public AProcessCollector startProcess(final String type, final String category) {
		final AProcessBuilder processBuilder = new AProcessBuilder(appName, type)
				.withLocation(location)
				.withCategory(category);
		push(processBuilder);
		return this;
	}

	/**
	 * Incrémentation d'une mesure du process courant (set si pas présente).
	 * @param measureType Nom de la mesure
	 * @param value Valeur
	 * @return this analytics process collector
	 */
	public AProcessCollector incMeasure(final String measureType, final double value) {
		peek().incMeasure(measureType, value);
		return this;
	}

	/**
	 * Annule et remplace une mesure du process courant.
	 * @param measureType Nom de la mesure
	 * @param value Valeur
	 * @return this analytics process collector
	 */
	public AProcessCollector setMeasure(final String measureType, final double value) {
		peek().setMeasure(measureType, value);
		return this;
	}

	/**
	 * Ajoute une meta-donnee du process courant (set si pas presente).
	 * TODO V0+ : voir si mutlivaluee interessante.
	 * @param metaDataName Nom de la meta donnee
	 * @param value Valeur
	 * @return this analytics process collector
	 */
	public AProcessCollector addMetaData(final String metaDataName, final String value) {
		peek().addMetaData(metaDataName, value);
		return this;
	}

	/**
	 * Termine le process courant.
	 * Le processus courant devient alors le processus parent le cas échéant.
	 * @return Process uniquement dans le cas ou c'est le processus parent.
	 */
	public Optional<AProcess> stopProcess() {
		final AProcess process = pop().build();
		if (getStack().isEmpty()) {
			//On est au processus racine on le collecte
			THREAD_LOCAL_PROCESS.remove(); //Et on le retire du ThreadLocal
			return Optional.of(process);
		}
		peek().addSubProcess(process);
		//On n'est pas dans le cas de la racine : conformement au contrat on renvoie null
		return Optional.empty();
	}
}
