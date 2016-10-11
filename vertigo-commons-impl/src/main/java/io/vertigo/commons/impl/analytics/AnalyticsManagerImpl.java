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

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsAgent;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracker;
import io.vertigo.commons.plugins.analytics.dummy.DummyAgentPlugin;
import io.vertigo.lang.Assertion;

/**
 * Main analytics manager implementation.
 *
 * @author pchretien
 */
public final class AnalyticsManagerImpl implements AnalyticsManager {
	private final AnalyticsAgentPlugin analyticsAgent;

	/**
	 * Constructor.
	 * @param agentPlugin Agent plugin used to report execution.
	 */
	@Inject
	public AnalyticsManagerImpl(final Optional<AnalyticsAgentPlugin> agentPlugin) {
		Assertion.checkNotNull(agentPlugin);
		//-----
		analyticsAgent = agentPlugin.orElse(new DummyAgentPlugin());
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
