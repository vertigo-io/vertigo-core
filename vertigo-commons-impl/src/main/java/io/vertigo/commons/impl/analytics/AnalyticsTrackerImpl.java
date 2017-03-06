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

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

import io.vertigo.commons.analytics.AnalyticsTracker;
import io.vertigo.lang.Assertion;

/**
 * Collect tracker.
 * @author npiedeloup
 */
final class AnalyticsTrackerImpl implements AnalyticsTracker, AutoCloseable {
	private Boolean succeeded; //default no info
	private Throwable causeException; //default no info
	private final Deque<AProcessBuilder> stack;
	private final Consumer<AProcess> consumer;

	/**
	 * Constructor.
	 * @param processType Process type (determine logger)
	 * @param category Category (identify action)
	 * @param createSubProcess if subProcess is created
	 * @param analyticsAgent Analytics agent to report execution
	 */
	AnalyticsTrackerImpl(
			final Optional<AnalyticsTrackerImpl> parentOpt,
			final String appLocation,
			final String processType,
			final String category,
			final Consumer<AProcess> consumer) {
		Assertion.checkArgNotEmpty(appLocation);
		Assertion.checkArgNotEmpty(processType);
		Assertion.checkArgNotEmpty(category);
		Assertion.checkNotNull(consumer);
		//---
		final AProcessBuilder processBuilder = new AProcessBuilder(processType)
				.withCategory(category);
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
	public AnalyticsTracker incMeasure(final String measureType, final double value) {
		stack.peek().incMeasure(measureType, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker setMeasure(final String measureType, final double value) {
		stack.peek().setMeasure(measureType, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker addMetaData(final String metaDataName, final String value) {
		stack.peek().addMetaData(metaDataName, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		if (succeeded != null) {
			setMeasure("success", succeeded ? 100 : 0);
		}
		if (causeException != null) {
			addMetaData("execption", causeException.getClass().getName());
		}
		final AProcess process = stack.pop().build();
		if (stack.isEmpty()) {
			//when the current process is the root process, it's finished and must be sent to the connector
			consumer.accept(process);
		} else {
			//when the current process is a subProcess, it's finished and must be added to the stack
			stack.peek().addSubProcess(process);
		}
	}

	/**
	 * Marks this tracker as succeeded.
	 * @return this tracker
	 */
	AnalyticsTracker markAsSucceeded() {
		//the last mark wins
		//so we prefer to reset causeException
		causeException = null;
		succeeded = true;
		return this;
	}

	/**
	 * Marks this tracker as Failed.
	 * @return this tracker
	 */
	AnalyticsTracker markAsFailed(final Throwable t) {
		//We don't check the nullability of e
		//the last mark wins
		//so we prefer to put the flag 'succeeded' to false
		succeeded = false;
		causeException = t;
		return this;
	}
}
