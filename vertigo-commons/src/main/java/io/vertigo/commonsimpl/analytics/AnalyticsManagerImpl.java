package io.vertigo.commonsimpl.analytics;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.kernel.lang.Option;
import io.vertigo.plugins.commons.analytics.dummy.DummyAgentPlugin;

import javax.inject.Inject;

/**
 * Impl�mentation de r�f�rence des fonctions Analytiques.
 * 
 * @author pchretien
 * @version $Id: AnalyticsManagerImpl.java,v 1.2 2013/10/22 12:35:20 pchretien Exp $
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
