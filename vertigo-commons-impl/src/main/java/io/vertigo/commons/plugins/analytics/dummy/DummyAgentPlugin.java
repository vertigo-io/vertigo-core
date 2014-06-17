package io.vertigo.commons.plugins.analytics.dummy;

import io.vertigo.commons.impl.analytics.AnalyticsAgentPlugin;

/**
 * Implémentation dummy de l'agent de collecte.
 * Cette implémentation ne fait RIEN.
 * @author pchretien
 */
public final class DummyAgentPlugin implements AnalyticsAgentPlugin {
	/** {@inheritDoc} */
	public void startProcess(final String processType, final String processName) {
		//
	}

	/** {@inheritDoc} */
	public void incMeasure(final String measureType, final double value) {
		//
	}

	/** {@inheritDoc} */
	public void setMeasure(final String measureType, final double value) {
		//
	}

	/** {@inheritDoc} */
	public void addMetaData(final String metaDataName, final String value) {
		//
	}

	/** {@inheritDoc} */
	public void stopProcess() {
		//
	}
}
