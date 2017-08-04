/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracer;
import io.vertigo.commons.daemon.DaemonScheduled;
import io.vertigo.commons.health.HealthCheck;
import io.vertigo.commons.health.HealthManager;
import io.vertigo.commons.metric.Metric;
import io.vertigo.commons.metric.MetricManager;
import io.vertigo.lang.Assertion;

/**
 * Main analytics manager implementation.
 *
 * @author pchretien
 */
public final class AnalyticsManagerImpl implements AnalyticsManager {

	private final HealthManager healthManager;
	private final MetricManager metricManager;
	private final List<AnalyticsConnectorPlugin> processConnectorPlugins;
	/**
	 * Processus binde sur le thread courant. Le processus , recoit les notifications des sondes placees dans le code de
	 * l'application pendant le traitement d'une requete (thread).
	 */
	private static final ThreadLocal<AnalyticsTracerImpl> THREAD_LOCAL_PROCESS = new ThreadLocal<>();

	private final boolean enabled;

	/**
	 * Constructor.
	 * @param processConnectorPlugins list of connectors to trace processes
	 */
	@Inject
	public AnalyticsManagerImpl(
			final HealthManager healthManager,
			final MetricManager metricManager,
			final List<AnalyticsConnectorPlugin> processConnectorPlugins) {
		Assertion.checkNotNull(processConnectorPlugins);
		//---
		this.healthManager = healthManager;
		this.metricManager = metricManager;
		this.processConnectorPlugins = processConnectorPlugins;
		// by default if no connector is defined we disable the collect
		enabled = !this.processConnectorPlugins.isEmpty();
	}

	/**
	 * Daemon to retrieve healthChecks and add them to the connectors
	 */
	@DaemonScheduled(name = "DMN_ANALYTICS_HEALTH", periodInSeconds = 10) //every hour
	public void sendHealthChecks() {
		if (enabled) {
			final List<HealthCheck> healthChecks = healthManager.getHealthChecks();
			processConnectorPlugins.forEach(
					connectorPlugin -> healthChecks.forEach(connectorPlugin::add));
		}
	}

	/**
	 * Daemon to retrieve metrics and add them to the connectors
	 */
	@DaemonScheduled(name = "DMN_ANALYTICS_METRIC", periodInSeconds = 60 * 60) //every hour
	public void sendMetrics() {
		if (enabled) {
			final List<Metric> metrics = metricManager.analyze();
			processConnectorPlugins.forEach(
					connectorPlugin -> metrics.forEach(connectorPlugin::add));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void trace(final String category, final String name, final Consumer<AnalyticsTracer> consumer) {
		try (AnalyticsTracerImpl tracer = createTracer(category, name)) {
			try {
				consumer.accept(tracer);
				tracer.markAsSucceeded();
			} catch (final Exception e) {
				tracer.markAsFailed(e);
				throw e;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public <O> O traceWithReturn(final String category, final String name, final Function<AnalyticsTracer, O> function) {
		try (AnalyticsTracerImpl tracer = createTracer(category, name)) {
			try {
				final O result = function.apply(tracer);
				tracer.markAsSucceeded();
				return result;
			} catch (final Exception e) {
				tracer.markAsFailed(e);
				throw e;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<AnalyticsTracer> getCurrentTracer() {
		if (!enabled) {
			return Optional.empty();
		}
		// When collect feature is enabled
		return doGetCurrentTracer().map(a -> a); // convert impl to api
	}

	private static Optional<AnalyticsTracerImpl> doGetCurrentTracer() {
		return Optional.ofNullable(THREAD_LOCAL_PROCESS.get());
	}

	private static void push(final AnalyticsTracerImpl analyticstracer) {
		Assertion.checkNotNull(analyticstracer);
		//---
		final Optional<AnalyticsTracerImpl> analyticstracerOptional = doGetCurrentTracer();
		if (!analyticstracerOptional.isPresent()) {
			THREAD_LOCAL_PROCESS.set(analyticstracer);
		}
	}

	private AnalyticsTracerImpl createTracer(final String category, final String name) {
		final Optional<AnalyticsTracerImpl> parent = doGetCurrentTracer();
		final AnalyticsTracerImpl analyticsTracer = new AnalyticsTracerImpl(parent, category, name, this::onClose);
		push(analyticsTracer);
		return analyticsTracer;
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
