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
package io.vertigo.commons.impl.analytics.process;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import io.vertigo.commons.analytics.process.ProcessAnalyticsTracer;
import io.vertigo.lang.Assertion;

/**
 * A tracer collectes information durint the execution of a process.
 * @author npiedeloup
 */
final class ProcessAnalyticsTracerImpl implements ProcessAnalyticsTracer, AutoCloseable {
	private final Logger logger;

	private final int processDeep;
	private final Optional<ProcessAnalyticsTracerImpl> parentOpt;
	private Boolean succeeded; //default no info
	private Throwable causeException; //default no info
	private final Consumer<AProcess> consumer;
	private final AProcessBuilder processBuilder;

	/**
	 * Constructor.
	 * @param parentOpt Optional Parent of this tracer
	 * @param category the category where the process is stored
	 * @param name the name that identified the process
	 * @param consumer Consumer of this process after closing
	 */
	ProcessAnalyticsTracerImpl(
			final Optional<ProcessAnalyticsTracerImpl> parentOpt,
			final String category,
			final String name,
			final Consumer<AProcess> consumer) {
		Assertion.checkArgNotEmpty(category);
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(consumer);
		//---
		logger = Logger.getLogger(category);
		this.parentOpt = parentOpt;
		this.consumer = consumer;

		processBuilder = AProcess.builder(category, name);
		if (parentOpt.isPresent()) {
			processDeep = parentOpt.get().processDeep;
			Assertion.checkState(processDeep < 100, "More than 100 process deep. All processes must be closed.");
		} else {
			processDeep = 0;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Start " + name);
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
		if (!parentOpt.isPresent()) {
			//when the current process is the root process, it's finished and must be sent to the connector
			consumer.accept(process);
		} else {
			//when the current process is a subProcess, it's finished and must be added to the parent
			parentOpt.get().processBuilder.addSubProcess(process);
		}
		logProcess(process);
	}

	private void logProcess(final AProcess process) {
		if (logger.isInfoEnabled()) {
			final StringBuilder sb = new StringBuilder()
					.append("Finish ")
					.append(process.getName())
					.append(succeeded ? " successfully" : " with error")
					.append(" in ( ")
					.append(process.getDurationMillis())
					.append(" ms)");
			if (!process.getMeasures().isEmpty()) {
				sb.append(" measures:").append(process.getMeasures());
			}
			if (!process.getTags().isEmpty()) {
				sb.append(" metaData:").append(process.getTags());
			}
			logger.info(sb.toString());
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
