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

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.commons.analytics.AnalyticsAgent;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracker;
import io.vertigo.lang.Assertion;

/**
 * Main analytics manager implementation.
 *
 * @author pchretien
 */
public final class AnalyticsManagerImpl implements AnalyticsManager {
	private final AnalyticsAgent analyticsAgent;

	/**
	 * Constructor.
	 */
	@Inject
	public AnalyticsManagerImpl(
			@Named("appName") final String appName,
			@Named("appLocation") final String appLocation,
			final AProcessConnectorPlugin processConnectorPlugin) {
		Assertion.checkNotNull(appName);
		Assertion.checkNotNull(appLocation);
		Assertion.checkNotNull(processConnectorPlugin);
		//---
		analyticsAgent = new AnalyticsAgentImpl(appName, appLocation, processConnectorPlugin);
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsAgent getAgent() {
		return analyticsAgent;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker startLogTracker(final String processType, final String category) {
		return new AnalyticsTrackerImpl(processType, category, false, analyticsAgent);
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker startTracker(final String processType, final String category) {
		return new AnalyticsTrackerImpl(processType, category, true, analyticsAgent);
	}

}
