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
import io.vertigo.commons.analytics.AnalyticsTrackerWritable;
import io.vertigo.lang.Assertion;

/**
 * Collect tracker.
 * @author npiedeloup
 */
final class AnalyticsTrackerImpl implements AnalyticsTrackerWritable {
	private final Deque<AProcessBuilder> stack;
	//	private boolean success;
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
		final AProcessBuilder processBuilder = new AProcessBuilder("app", processType)
				.withLocation(appLocation)
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
		//on ne place pas cette mesure si pas de process local
		//analyticsAgent.setMeasure("errorPct", success ? 0 : 100);
		final AProcess process = stack.pop().build();
		if (stack.isEmpty()) {
			//case of the root process, it's finished and must be sent to the connector
			consumer.accept(process);
		} else {
			//case of a subProcess, it's finished and must be added to the stack
			stack.peek().addSubProcess(process);
		}
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker markAsSucceeded() {
		//success = true;
		return this;
	}

	@Override
	public AnalyticsTracker markAsFailed(final Exception e) {
		// TODO Auto-generated method stub
		return this;
	}
}
