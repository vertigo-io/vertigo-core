/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
 */
package io.vertigo.commons.plugins.analytics.analytica;

import java.net.UnknownHostException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.commons.impl.analytics.AnalyticsAgentPlugin;
import io.vertigo.commons.plugins.analytics.analytica.connector.AProcessConnector;
import io.vertigo.commons.plugins.analytics.analytica.connector.LoggerConnector;
import io.vertigo.commons.plugins.analytics.analytica.process.AProcess;
import io.vertigo.commons.plugins.analytics.analytica.process.AProcessBuilder;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Impl�mentation de l'agent de collecte avec redirection vers Analytica.
 * @author pchretien, npiedeloup
 * @version $Id: AnalyticaAgentPlugin.java,v 1.6 2012/05/10 09:38:14 npiedeloup Exp $
 */
public final class AnalyticaAgentPlugin implements AnalyticsAgentPlugin {
	/**
	 * Processus binde sur le thread courant. Le processus , recoit les notifications des sondes placees dans le code de
	 * l'application pendant le traitement d'une requete (thread).
	 */
	private static final ThreadLocal<Deque<AProcessBuilder>> THREAD_LOCAL_PROCESS = new ThreadLocal<>();

	private static final String KEY_HOST_NAME = "\\{java.io.hostName\\}";
	private final String appName;
	private final String location;

	private final AProcessConnector processConnector;

	/**
	 * Constructeur.
	 * @param systemName System name
	 * @param systemLocation System location (Environment, Server, Jvm, ..)
	 */
	@Inject
	public AnalyticaAgentPlugin(@Named("systemName") final String systemName, @Named("systemLocation") final String systemLocation) {
		processConnector = new LoggerConnector();
		appName = systemName;
		location = translateSystemLocation(systemLocation);
	}

	private static String translateSystemLocation(final String systemLocation) {
		try {
			return systemLocation.replaceAll(KEY_HOST_NAME, java.net.InetAddress.getLocalHost().getHostName());
		} catch (final UnknownHostException e) {
			throw new WrappedException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void startProcess(final String type, final String category) {
		final AProcessBuilder processBuilder = new AProcessBuilder(appName, type)
				.withLocation(location)
				.withCategory(category);
		push(processBuilder);
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
		} else {
			Assertion.checkState(stack.size() < 100, "the stack of AProcesses contains more than 100 process. All processes must be closed.\nStack:" + stack);
		}
		stack.push(processBuilder);
	}

	/** {@inheritDoc} */
	@Override
	public void incMeasure(final String measureType, final double value) {
		getStack().peek()
				.incMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void setMeasure(final String measureType, final double value) {
		getStack().peek()
				.setMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void addMetaData(final String metaDataName, final String value) {
		getStack().peek()
				.addMetaData(metaDataName, value);
	}

	/**
	 * Termine le process courant.
	 * Le processus courant devient alors le processus parent le cas échéant.
	 * @return Process uniquement dans le cas ou c'est le processus parent.
	 */
	private Optional<AProcess> doStopProcess() {
		final AProcess process = getStack().pop()
				.build();
		if (getStack().isEmpty()) {
			//On est au processus racine on le collecte
			THREAD_LOCAL_PROCESS.remove(); //Et on le retire du ThreadLocal
			return Optional.of(process);
		}
		getStack().peek()
				.addSubProcess(process);
		//On n'est pas dans le cas de la racine
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public void stopProcess() {
		doStopProcess()
				.ifPresent(processConnector::add);
	}
}
