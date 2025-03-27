/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.impl.analytics.trace;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.analytics.trace.TraceSpan;
import io.vertigo.core.analytics.trace.TraceSpanBuilder;
import io.vertigo.core.analytics.trace.Tracer;
import io.vertigo.core.lang.Assertion;

/**
 * A tracer collects information during the execution of a process.
 *
 * @author npiedeloup
 */
final class TracerImpl implements Tracer, AutoCloseable {
	private final Logger logger;

	private int logStackTraceCounter = 0;

	private final Consumer<TraceSpan> consumer;
	private final Supplier<Optional<TracerImpl>> parentOptSupplier;
	private final TraceSpanBuilder spanBuilder;

	/**
	 * Constructor.
	 *
	 * @param parentOpt Optional Parent of this tracer
	 * @param category the category where the process is stored
	 * @param name the name that identified the process
	 * @param consumer Consumer of this process after closing
	 */
	TracerImpl(
			final String category,
			final String name,
			final Consumer<TraceSpan> consumer,
			final Supplier<Optional<TracerImpl>> parentOptSupplier) {
		Assertion.check()
				.isNotBlank(category)
				.isNotBlank(name)
				.isNotNull(consumer)
				.isNotNull(parentOptSupplier);
		//---
		logger = LogManager.getLogger(category);
		this.consumer = consumer;
		this.parentOptSupplier = parentOptSupplier;

		spanBuilder = TraceSpan.builder(category, name);
		if (logger.isDebugEnabled()) {
			logger.debug("Start {}", name);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Tracer incMeasure(final String name, final double value) {
		spanBuilder.incMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Tracer setMeasure(final String name, final double value) {
		spanBuilder.withMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Tracer setMetadata(final String name, final String value) {
		spanBuilder.withMetadata(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Tracer setTag(final String name, final String value) {
		spanBuilder.withTag(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		final TraceSpan span = spanBuilder.build();

		final Optional<TracerImpl> parentOpt = parentOptSupplier.get();
		if (parentOpt.isPresent()) {
			//when the current process is a subProcess, it's finished and must be added to the parent
			parentOpt.get().spanBuilder.addChildSpan(span);
		} else {
			try {
				//when the current process is the root process, it's finished and must be sent to the connector
				consumer.accept(span);
			} catch (final Throwable th) {//catch Throwable : We must ensure there is no exception here : it will be loose
				if (logStackTraceCounter % 100 == 0) {
					logger.warn("Error while closing process (error in consumer " + consumer.getClass().getName() + ").", th);
				} else {
					logger.warn("Error while closing process (error in consumer {}).", consumer.getClass().getName());
				}
				logStackTraceCounter = logStackTraceCounter++ % 100;
			}
		}
	}

	/**
	 * Marks this tracer as succeeded.
	 *
	 * @return this tracer
	 */
	Tracer markAsSucceeded() {
		spanBuilder.markAsSucceeded();
		return this;
	}

	/**
	 * Marks this tracer as Failed.
	 *
	 * @return this tracer
	 */
	Tracer markAsFailed(final Throwable t) {
		spanBuilder.markAsFailed(t);
		return this;
	}
}
