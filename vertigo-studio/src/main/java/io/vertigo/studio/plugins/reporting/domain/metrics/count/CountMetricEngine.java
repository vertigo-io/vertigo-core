package io.vertigo.studio.plugins.reporting.domain.metrics.count;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.Metric;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Comptage du nombre de lignes.
 *
 * @author pchretien
 */
public final class CountMetricEngine implements MetricEngine<DtDefinition, CountMetric> {
	private final PersistenceManager persistenceManager;

	/**
	 * Constructeur.
	 * @param persistenceManager Manager de persistance
	 */
	public CountMetricEngine(final PersistenceManager persistenceManager) {
		Assertion.checkNotNull(persistenceManager);
		//---------------------------------------------------------------------
		this.persistenceManager = persistenceManager;
	}

	/** {@inheritDoc} */
	public CountMetric execute(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---------------------------------------------------------------------
		if (!dtDefinition.isPersistent()) {
			return new CountMetric(null, Metric.Status.Rejected);
		}
		//Dans le cas ou DT est persistant on compte le nombre de lignes.
		try {
			final int count = persistenceManager.getBroker().count(dtDefinition);
			return new CountMetric(count, Metric.Status.Executed);
		} catch (final Exception e) {
			return new CountMetric(null, Metric.Status.Error);
		}
	}
}
