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

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.commons.impl.analytics.AnalyticsAgentPlugin;
import io.vertigo.commons.plugins.analytics.analytica.connector.AProcessCollector;
import io.vertigo.commons.plugins.analytics.analytica.connector.AProcessConnector;
import io.vertigo.commons.plugins.analytics.analytica.connector.LoggerConnector;
import io.vertigo.lang.WrappedException;

/**
 * Impl�mentation de l'agent de collecte avec redirection vers Analytica.
 * @author pchretien, npiedeloup
 * @version $Id: AnalyticaAgentPlugin.java,v 1.6 2012/05/10 09:38:14 npiedeloup Exp $
 */
public final class AnalyticaAgentPlugin implements AnalyticsAgentPlugin {
	private static final String KEY_HOST_NAME = "\\{java.io.hostName\\}";

	private final AProcessCollector processCollector;

	/**
	 * Constructeur.
	 * @param systemName System name
	 * @param systemLocation System location (Environment, Server, Jvm, ..)
	 */
	@Inject
	public AnalyticaAgentPlugin(@Named("systemName") final String systemName, @Named("systemLocation") final String systemLocation) {
		super();
		//-----------------------------------------------------------------
		final AProcessConnector processConnector = new LoggerConnector();
		final String mySystemLocation = translateSystemLocation(systemLocation);
		processCollector = new AProcessCollector(systemName, mySystemLocation, processConnector);
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
	public void startProcess(final String processType, final String category) {
		processCollector.startProcess(processType, category);
	}

	/** {@inheritDoc} */
	@Override
	public void incMeasure(final String measureType, final double value) {
		processCollector.incMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void setMeasure(final String measureType, final double value) {
		processCollector.setMeasure(measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void addMetaData(final String metaDataName, final String value) {
		processCollector.addMetaData(metaDataName, value);
	}

	/** {@inheritDoc} */
	@Override
	public void stopProcess() {
		processCollector.stopProcess();
	}

}
