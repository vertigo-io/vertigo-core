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

import java.util.HashMap;
import java.util.Map;

import io.vertigo.commons.analytics.AnalyticsAgent;
import io.vertigo.commons.analytics.AnalyticsTracker;
import io.vertigo.lang.Assertion;

/**
 * Collect tracker.
 * @author npiedeloup
 */
final class AnalyticsTrackerImpl implements AnalyticsTracker {
	private final AnalyticsAgent analyticsAgent;

	//Tableau des mesures identifiées par leur nom.
	private final Map<String, Double> measures = new HashMap<>();

	//Tableau des métadonnées identifiées par leur nom.
	private final Map<String, String> metaData = new HashMap<>();

	private boolean success;

	/**
	 * Constructor.
	 * @param processType Process type (determine logger)
	 * @param category Category (identify action)
	 * @param createSubProcess if subProcess is created
	 * @param analyticsAgent Analytics agent to report execution
	 */
	AnalyticsTrackerImpl(final String processType, final String category, final AnalyticsAgent analyticsAgent) {
		Assertion.checkArgNotEmpty(processType);
		Assertion.checkArgNotEmpty(category);
		Assertion.checkNotNull(analyticsAgent);
		//---
		this.analyticsAgent = analyticsAgent;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker incMeasure(final String measureType, final double value) {
		final Double prevValue = measures.get(measureType);
		measures.put(measureType, prevValue != null ? (prevValue + value) : value);
		analyticsAgent.incMeasure(measureType, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker setMeasure(final String measureType, final double value) {
		measures.put(measureType, value);
		analyticsAgent.setMeasure(measureType, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker addMetaData(final String metaDataName, final String value) {
		metaData.put(metaDataName, value);
		analyticsAgent.addMetaData(metaDataName, value);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker markAsSucceeded() {
		success = true;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		//on ne place pas cette mesure si pas de process local
		analyticsAgent.setMeasure("errorPct", success ? 0 : 100);
		analyticsAgent.stopProcess();
	}

}
