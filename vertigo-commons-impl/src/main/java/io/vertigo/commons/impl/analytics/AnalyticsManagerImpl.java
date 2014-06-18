package io.vertigo.commons.impl.analytics;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.plugins.analytics.dummy.DummyAgentPlugin;
import io.vertigo.kernel.lang.Option;

import javax.inject.Inject;

/**
 * Implémentation de référence des fonctions Analytiques.
 * 
 * @author pchretien
 */
public final class AnalyticsManagerImpl implements AnalyticsManager {
	@Inject
	private Option<AnalyticsAgentPlugin> agentPlugin;

	private final AnalyticsAgentPlugin defaultAgentPlugin = new DummyAgentPlugin();

	/** {@inheritDoc} */
	public AnalyticsAgentPlugin getAgent() {
		return agentPlugin.getOrElse(defaultAgentPlugin);
	}
}
