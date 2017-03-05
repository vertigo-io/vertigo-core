/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.impl.analytics;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracker;
import io.vertigo.commons.analytics.AnalyticsTrackerWritable;
import io.vertigo.commons.plugins.analytics.connector.LoggerProcessConnectorPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Main analytics manager implementation.
 *
 * @author pchretien
 */
public final class AnalyticsManagerImpl implements AnalyticsManager {
	private final List<AProcessConnectorPlugin> processConnectorPlugins;
	/**
	 * Processus binde sur le thread courant. Le processus , recoit les notifications des sondes placees dans le code de
	 * l'application pendant le traitement d'une requete (thread).
	 */
	private static final ThreadLocal<AnalyticsTrackerImpl> THREAD_LOCAL_PROCESS = new ThreadLocal<>();

	/**
	 * Constructor.
	 */
	@Inject
	public AnalyticsManagerImpl(final List<AProcessConnectorPlugin> processConnectorPlugins) {
		Assertion.checkNotNull(processConnectorPlugins);
		//---
		//pout tester >>
		//pout tester >>
		//pout tester >>
		this.processConnectorPlugins = Collections.singletonList(new LoggerProcessConnectorPlugin());
		//		this.processConnectorPlugins = processConnectorPlugins;
	}

	@Override
	public Optional<AnalyticsTracker> getCurrentTracker() {
		final Optional<AnalyticsTrackerImpl> analyticsTrackerOpt = doGetCurrentTracker();
		if (analyticsTrackerOpt.isPresent()) {
			return Optional.of(analyticsTrackerOpt.get());
		}
		return Optional.empty();
	}

	private static Optional<AnalyticsTrackerImpl> doGetCurrentTracker() {
		return Optional.ofNullable(THREAD_LOCAL_PROCESS.get());
	}

	private static void push(final AnalyticsTrackerImpl analyticsTracker) {
		Assertion.checkNotNull(analyticsTracker);
		//---
		final Optional<AnalyticsTrackerImpl> analyticsTrackerOptional = doGetCurrentTracker();
		if (!analyticsTrackerOptional.isPresent()) {
			THREAD_LOCAL_PROCESS.set(analyticsTracker);
		}
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTrackerWritable createTracker(final String processType, final String category) {
		final Optional<AnalyticsTrackerImpl> parent = doGetCurrentTracker();
		final AnalyticsTrackerImpl analyticsTracker = new AnalyticsTrackerImpl(parent, getHostName(), processType, category, this::onClose);
		push(analyticsTracker);
		return analyticsTracker;
	}

	private static String getHostName() {
		try {
			return java.net.InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			throw new WrappedException(e);
		}
	}

	private void onClose(final AProcess process) {
		Assertion.checkNotNull(process);
		//---
		//1.
		THREAD_LOCAL_PROCESS.remove();
		//2.
		processConnectorPlugins.forEach(
				processConnectorPlugin -> processConnectorPlugin.add(process));
	}
}
