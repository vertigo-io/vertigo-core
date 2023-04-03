/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
 * @author npiedeloup
 */
final class TracerImpl implements Tracer, AutoCloseable {
	private final Logger logger;

	private int logStackTraceCounter = 0;

	private Boolean succeeded; //default no info
	private Throwable causeException; //default no info
	private final Consumer<TraceSpan> consumer;
	private final Supplier<Optional<TracerImpl>> parentOptSupplier;
	private final TraceSpanBuilder spanBuilder;

	/**
	 * Constructor.
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
		if (succeeded != null) {
			setMeasure("success", succeeded ? 100 : 0);
		}
		if (causeException != null) {
			setTag("exception", causeException.getClass().getName());
		}
		final TraceSpan span = spanBuilder.build();
		logSpan(span);

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
					logger.warn("Error while closing process (error in consumer {0}).", consumer.getClass().getName());
				}
				logStackTraceCounter = logStackTraceCounter++ % 100;
			}
		}
	}

	private void logSpan(final TraceSpan span) {
		if (logger.isInfoEnabled()) {
			final boolean hasMeasures = !span.getMeasures().isEmpty();
			final boolean hasMetadatas = !span.getMetadatas().isEmpty();
			final boolean hasTags = !span.getTags().isEmpty();
			final String info = new StringBuilder()
					.append("Finish ")
					.append(span.getName())
					.append(succeeded != null ? succeeded ? " successfully" : " with error" : "with internal error")
					.append(" in ( ")
					.append(span.getDurationMillis())
					.append(" ms)")
					.append(hasMeasures ? " measures:" + span.getMeasures() : "")
					.append(hasMetadatas ? " metadatas:" + span.getMetadatas() : "")
					.append(hasTags ? " tags:" + span.getTags() : "")
					.toString();
			logger.info(info);
		}

	}

	/**
	 * Marks this tracer as succeeded.
	 * @return this tracer
	 */
	Tracer markAsSucceeded() {
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
	Tracer markAsFailed(final Throwable t) {
		//We don't check the nullability of e
		//the last mark wins
		//so we prefer to put the flag 'succeeded' to false
		succeeded = false;
		causeException = t;
		return this;
	}
}
