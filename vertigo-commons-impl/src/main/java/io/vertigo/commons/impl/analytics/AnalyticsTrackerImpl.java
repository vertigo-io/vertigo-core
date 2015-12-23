/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import io.vertigo.commons.analytics.AnalyticsTracker;
import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Collect tracker.
 * @author npiedeloup
 */
public final class AnalyticsTrackerImpl implements AnalyticsTracker {

	private final Logger logger;
	private final String processType;
	private final String category;
	private final String analyticsMeasurePrefix;
	private final boolean createSubProcess;
	private final AnalyticsAgentPlugin analyticsAgent;

	//Tableau des mesures identifiées par leur nom.
	private final Map<String, Double> measures = new HashMap<>();

	//Tableau des métadonnées identifiées par leur nom.
	private final Map<String, String> metaData = new HashMap<>();

	private final long start;

	private boolean success;

	/**
	 * Constructor.
	 * @param processType Process type (determine logger)
	 * @param category Category (identify action)
	 * @param createSubProcess if subProcess is created
	 * @param analyticsAgent Analytics agent to report execution
	 */
	AnalyticsTrackerImpl(final String processType, final String category, final boolean createSubProcess, final AnalyticsAgentPlugin analyticsAgent) {
		Assertion.checkArgNotEmpty(processType);
		Assertion.checkArgNotEmpty(category);
		Assertion.checkNotNull(analyticsAgent);
		this.processType = processType;
		this.category = category;
		this.createSubProcess = createSubProcess;
		this.analyticsAgent = analyticsAgent;
		logger = Logger.getLogger(processType);
		analyticsMeasurePrefix = createSubProcess ? "" : processType;
		start = System.currentTimeMillis();
		if (createSubProcess) {
			analyticsAgent.startProcess(processType, category);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Start " + category);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void incMeasure(final String measureType, final double value) {
		final Double prevValue = measures.get(measureType);
		measures.put(measureType, prevValue != null ? (prevValue + value) : value);
		analyticsAgent.incMeasure(analyticsMeasurePrefix + measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void setMeasure(final String measureType, final double value) {
		measures.put(measureType, value);
		analyticsAgent.setMeasure(analyticsMeasurePrefix + measureType, value);
	}

	/** {@inheritDoc} */
	@Override
	public void addMetaData(final String metaDataName, final String value) {
		metaData.put(metaDataName, value);
		analyticsAgent.addMetaData(analyticsMeasurePrefix + metaDataName, value);
	}

	/** {@inheritDoc} */
	@Override
	public void markAsSucceeded() {
		success = true;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		final long duration = System.currentTimeMillis() - start;
		if (createSubProcess) {
			//on ne place pas cette mesure si pas de process local
			analyticsAgent.setMeasure("errorPct", success ? 0 : 100);
			analyticsAgent.stopProcess();
		} else {
			analyticsAgent.incMeasure(processType + "Count", 1);
			analyticsAgent.incMeasure(processType + "Duration", duration);
		}
		if (logger.isInfoEnabled()) {
			final StringBuilder sb = new StringBuilder()
					.append("Finish ")
					.append(category);
			if (success) {
				sb.append(" successfully in  ( ");
			} else {
				sb.append(" with error in ( ");
			}
			sb.append(duration);
			sb.append(" ms)");
			if (measures.isEmpty()) {
				sb.append(" measures:").append(measures);
			}
			if (metaData.isEmpty()) {
				sb.append(" metaData:").append(metaData);
			}
			logger.info(sb.toString());
		}
	}

}
