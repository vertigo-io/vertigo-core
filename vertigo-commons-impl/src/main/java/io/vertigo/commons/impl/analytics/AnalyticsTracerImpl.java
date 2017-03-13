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

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracer;
import io.vertigo.lang.Assertion;

/**
 * A tracer collectes information durint the execution of a process.
 * @author npiedeloup
 */
final class AnalyticsTracerImpl implements AnalyticsTracer, AutoCloseable {
	private final static Logger LOGGER = Logger.getLogger(AnalyticsManager.class);

	private Boolean succeeded; //default no info
	private Throwable causeException; //default no info
	private final Deque<AProcessBuilder> stack;
	private final Consumer<AProcess> consumer;

	/**
	 * Constructor.
	 * @param category the category where the process is stored
	 * @param name the name that identified the perocess
	 * @param createSubProcess if subProcess is created
	 * @param analyticsAgent Analytics agent to report execution
	 */
	AnalyticsTracerImpl(
			final Optional<AnalyticsTracerImpl> parentOpt,
			final String category,
			final String name,
			final Consumer<AProcess> consumer) {
		Assertion.checkArgNotEmpty(category);
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(consumer);
		//---
		final AProcessBuilder processBuilder = new AProcessBuilder(category, name);
		this.consumer = consumer;
		if (parentOpt.isPresent()) {
			stack = parentOpt.get().stack;
			Assertion.checkState(stack.size() < 100, "the stack contains more than 100 process. All processes must be closed.\nStack:" + stack);
		} else {
			stack = new LinkedList<>();
		}
		stack.push(processBuilder);
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracer incMeasure(final String name, final double value) {
		stack.peek().incMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracer setMeasure(final String name, final double value) {
		stack.peek().setMeasure(name, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracer addTag(final String name, final String value) {
		stack.peek().addTag(name, value);
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
		final AProcess process = stack.pop().build();
		if (stack.isEmpty()) {
			//when the current process is the root process, it's finished and must be sent to the connector
			consumer.accept(process);
		} else {
			//when the current process is a subProcess, it's finished and must be added to the stack
			stack.peek().addSubProcess(process);
		}
		logProcess(process, succeeded);
	}

	private static void logProcess(final AProcess process, final boolean succeeded) {
		if (LOGGER.isInfoEnabled()) {
			final StringBuilder sb = new StringBuilder()
					.append("Finish ")
					.append(process.getCategory())
					.append(succeeded ? " successfully" : " with error").append(" in ( ")
					.append(process.getDurationMillis())
					.append(" ms)");
			if (!process.getMeasures().isEmpty()) {
				sb.append(" measures:").append(process.getMeasures());
			}
			if (!process.getTags().isEmpty()) {
				sb.append(" metaData:").append(process.getTags());
			}
			LOGGER.info(sb.toString());
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
