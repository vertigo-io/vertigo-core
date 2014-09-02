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

import io.vertigo.commons.analytics.AnalyticsAgent;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.plugins.analytics.dummy.DummyAgentPlugin;
import io.vertigo.core.lang.Option;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;

/**
 * Implémentation de référence des fonctions Analytiques.
 * 
 * @author pchretien
 */
public final class AnalyticsManagerImpl implements AnalyticsManager {
	private final AnalyticsAgent analyticsAgent;

	@Inject
	public AnalyticsManagerImpl(final Option<AnalyticsAgentPlugin> agentPlugin) {
		Assertion.checkNotNull(agentPlugin);
		//---------------------------------------------------------------------
		this.analyticsAgent = agentPlugin.getOrElse(new DummyAgentPlugin());
	}

	/** {@inheritDoc} */
	public AnalyticsAgent getAgent() {
		return analyticsAgent;
	}
}
