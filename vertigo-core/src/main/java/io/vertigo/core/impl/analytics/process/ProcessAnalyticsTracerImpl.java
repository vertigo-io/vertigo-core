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
package io.vertigo.core.impl.analytics.process;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.analytics.process.AProcess;
import io.vertigo.core.analytics.process.AProcessBuilder;
import io.vertigo.core.analytics.process.ProcessAnalyticsTracer;
import io.vertigo.core.lang.Assertion;

/**
 * A tracer collectes information durint the execution of a process.
 * @author npiedeloup
 */
final class ProcessAnalyticsTracerImpl implements ProcessAnalyticsTracer, AutoCloseable {
	private final Logger logger;

	private int logStackTraceCounter = 0;

	private Boolean succeeded; //default no info
	private Throwable causeException; //default no info
	private final Consumer<AProcess> consumer;
	private final Supplier<Optional<ProcessAnalyticsTracerImpl>> parentOptSupplier;
	private final AProcessBuilder processBuilder;

	/**
	 * Constructor.
	 * @param parentOpt Optional Parent of this tracer
	 * @param category the category where the process is stored
	 * @param name the name that identified the process
	 * @param consumer Consumer of this process after closing
	 */
	ProcessAnalyticsTracerImpl(
			final String category,
			final String name,
			final Consumer<AProcess> consumer,
			final Supplier<Optional<ProcessAnalyticsTracerImpl>> parentOptSupplier) {
		Assertion.check()
				.isNotBlank(category)
				.isNotBlank(name)
				.isNotNull(consumer)
				.isNotNull(parentOptSupplier);
		//---
		logger = LogManager.getLogger(category);
		this.consumer = consumer;
		this.parentOptSupplier = parentOptSupplier;

		processBuilder = AProcess.builder(category, name);
		if (logger.isDebugEnabled()) {
			logger.debug("Start {}", name);
		}
	}

	/** {@inheritDoc} */
	@Override
	public ProcessAnalyticsTracer incMeasure(final String name, final double value) {
		processBuilder.incMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ProcessAnalyticsTracer setMeasure(final String name, final double value) {
		processBuilder.setMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ProcessAnalyticsTracer addMetadata(final String name, final String value) {
		processBuilder.addMetadata(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ProcessAnalyticsTracer addTag(final String name, final String value) {
		processBuilder.addTag(name, value);
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
		final AProcess process = processBuilder.build();
		logProcess(process);

		final Optional<ProcessAnalyticsTracerImpl> parentOpt = parentOptSupplier.get();
		if (parentOpt.isPresent()) {
			//when the current process is a subProcess, it's finished and must be added to the parent
			parentOpt.get().processBuilder.addSubProcess(process);
		} else {
			try {
				//when the current process is the root process, it's finished and must be sent to the connector
				consumer.accept(process);
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

	private void logProcess(final AProcess process) {
		if (logger.isInfoEnabled()) {
			final boolean hasMeasures = !process.getMeasures().isEmpty();
			final boolean hasMetadatas = !process.getMetadatas().isEmpty();
			final boolean hasTags = !process.getTags().isEmpty();
			final String info = new StringBuilder()
					.append("Finish ")
					.append(process.getName())
					.append(succeeded != null ? succeeded ? " successfully" : " with error" : "with internal error")
					.append(" in ( ")
					.append(process.getDurationMillis())
					.append(" ms)")
					.append(hasMeasures ? " measures:" + process.getMeasures() : "")
					.append(hasMetadatas ? " metadatas:" + process.getMetadatas() : "")
					.append(hasTags ? " tags:" + process.getTags() : "")
					.toString();
			logger.info(info);
		}

	}

	/**
	 * Marks this tracer as succeeded.
	 * @return this tracer
	 */
	ProcessAnalyticsTracer markAsSucceeded() {
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
	ProcessAnalyticsTracer markAsFailed(final Throwable t) {
		//We don't check the nullability of e
		//the last mark wins
		//so we prefer to put the flag 'succeeded' to false
		succeeded = false;
		causeException = t;
		return this;
	}
}
