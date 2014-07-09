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
