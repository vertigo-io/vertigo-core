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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracer;
import io.vertigo.lang.Assertion;

/**
 * Main analytics manager implementation.
 *
 * @author pchretien
 */
public final class AnalyticsManagerImpl implements AnalyticsManager {
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
	public AnalyticsManagerImpl(final List<AnalyticsConnectorPlugin> processConnectorPlugins) {
		Assertion.checkNotNull(processConnectorPlugins);
		//---
		this.processConnectorPlugins = processConnectorPlugins;
		// by default if no connector is defined we disable the collect
		enabled = !this.processConnectorPlugins.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public void trace(final String channel, final String category, final Consumer<AnalyticsTracer> consumer) {
		if (!enabled) {
			consumer.accept(AnalyticsTracerDummy.DUMMY_TRACER);
		} else {
			// When collect feature is enabled
			try (AnalyticsTracerImpl tracer = createTracer(channel, category)) {
				try {
					consumer.accept(tracer);
					tracer.markAsSucceeded();
				} catch (final Exception e) {
					tracer.markAsFailed(e);
					throw e;
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public <O> O traceWithReturn(final String channel, final String category, final Function<AnalyticsTracer, O> function) {
		if (!enabled) {
			return function.apply(AnalyticsTracerDummy.DUMMY_TRACER);
		}
		// When collect feature is enabled
		try (AnalyticsTracerImpl tracer = createTracer(channel, category)) {
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
		final Optional<AnalyticsTracerImpl> analyticstracerOpt = doGetCurrentTracer();
		if (analyticstracerOpt.isPresent()) {
			return Optional.of(analyticstracerOpt.get());
		}
		return Optional.empty();
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

	private AnalyticsTracerImpl createTracer(final String channel, final String category) {
		final Optional<AnalyticsTracerImpl> parent = doGetCurrentTracer();
		final AnalyticsTracerImpl analyticstracer = new AnalyticsTracerImpl(parent, channel, category, this::onClose);
		push(analyticstracer);
		return analyticstracer;
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
