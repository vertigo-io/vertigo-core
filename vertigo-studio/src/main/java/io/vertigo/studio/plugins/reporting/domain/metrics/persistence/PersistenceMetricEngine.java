package io.vertigo.studio.plugins.reporting.domain.metrics.persistence;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Vérifier si le DT est persistant.
 *
 * @author pchretien
 */
public final class PersistenceMetricEngine implements MetricEngine<DtDefinition, PersitenceMetric> {
	private final PersistenceManager persistenceManager;

	/**
	 * Constructeur.
	 * @param persistenceManager Manager de persistance
	 */
	public PersistenceMetricEngine(final PersistenceManager persistenceManager) {
		Assertion.checkNotNull(persistenceManager);
		//---------------------------------------------------------------------
		this.persistenceManager = persistenceManager;
	}

	/** {@inheritDoc} */
	public PersitenceMetric execute(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---------------------------------------------------------------------
		return new PersitenceMetric(dtDefinition.isPersistent(), test(dtDefinition));
	}

	//On teste si la définition est persistante, elle existe en BDD et le mapping est ok.
	private boolean test(final DtDefinition dtDefinition) {
		if (!dtDefinition.isPersistent()) {
			return true;
		}
		try {
			persistenceManager.getBroker().getList(dtDefinition, null, 1);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}
}
