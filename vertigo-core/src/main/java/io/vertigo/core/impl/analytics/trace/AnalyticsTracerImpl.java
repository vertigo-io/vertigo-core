/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2021, Vertigo.io, team@vertigo.io
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

import io.vertigo.core.analytics.trace.AnalyticsSpan;
import io.vertigo.core.analytics.trace.AnalyticsSpanBuilder;
import io.vertigo.core.analytics.trace.AnalyticsTracer;
import io.vertigo.core.lang.Assertion;

/**
 * A tracer collectes information durint the execution of a process.
 * @author npiedeloup
 */
final class AnalyticsTracerImpl implements AnalyticsTracer, AutoCloseable {
	private final Logger logger;

	private Boolean succeeded; //default no info
	private Throwable causeException; //default no info
	private final Consumer<AnalyticsSpan> consumer;
	private final Supplier<Optional<AnalyticsTracerImpl>> parentOptSupplier;
	private final AnalyticsSpanBuilder spanBuilder;

	/**
	 * Constructor.
	 * @param parentOpt Optional Parent of this tracer
	 * @param category the category where the process is stored
	 * @param name the name that identified the process
	 * @param consumer Consumer of this process after closing
	 */
	AnalyticsTracerImpl(
			final String category,
			final String name,
			final Consumer<AnalyticsSpan> consumer,
			final Supplier<Optional<AnalyticsTracerImpl>> parentOptSupplier) {
		Assertion.check()
				.isNotBlank(category)
				.isNotBlank(name)
				.isNotNull(consumer);
		//---
		logger = LogManager.getLogger(category);
		this.consumer = consumer;
		this.parentOptSupplier = parentOptSupplier;

		spanBuilder = AnalyticsSpan.builder(category, name);
		if (logger.isDebugEnabled()) {
			logger.debug("Start {}", name);
		}
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracer incMeasure(final String name, final double value) {
		spanBuilder.incMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracer setMeasure(final String name, final double value) {
		spanBuilder.withMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracer addTag(final String name, final String value) {
		spanBuilder.withTag(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		if (succeeded != null) {
			setMeasure("success", succeeded ? 100 : 0);
		}
		if (causeException != null) {
			addTag("exception", causeException.getClass().getName());
		}
		final AnalyticsSpan span = spanBuilder.build();
		logSpan(span);

		final Optional<AnalyticsTracerImpl> parentOpt = parentOptSupplier.get();
		if (parentOpt.isPresent()) {
			//when the current process is a subProcess, it's finished and must be added to the parent
			parentOpt.get().spanBuilder.addChildSpan(span);
		} else {
			//when the current process is the root process, it's finished and must be sent to the connector
			consumer.accept(span);
		}
	}

	private void logSpan(final AnalyticsSpan span) {
		if (logger.isInfoEnabled()) {
			final boolean hasMeasures = !span.getMeasures().isEmpty();
			final boolean hasTags = !span.getTags().isEmpty();
			final String info = new StringBuilder()
					.append("Finish ")
					.append(span.getName())
					.append(succeeded != null ? succeeded ? " successfully" : " with error" : "with internal error")
					.append(" in ( ")
					.append(span.getDurationMillis())
					.append(" ms)")
					.append(hasMeasures ? " measures:" + span.getMeasures() : "")
					.append(hasTags ? " metaData:" + span.getTags() : "")
					.toString();
			logger.info(info);
		}

	}

	/**
	 * Marks this tracer as succeeded.
	 * @return this tracer
	 */
	AnalyticsTracer markAsSucceeded() {
		//the last mark wins
		//so we prefer to reset causeException
		causeException = null;
		succeeded = true;
		return this;
	}

	/**
	 * Marks this tracer as Failed.
	 * @return this tracer
	 */
	AnalyticsTracer markAsFailed(final Throwable t) {
		//We don't check the nullability of e
		//the last mark wins
		//so we prefer to put the flag 'succeeded' to false
		succeeded = false;
		causeException = t;
		return this;
	}
}
