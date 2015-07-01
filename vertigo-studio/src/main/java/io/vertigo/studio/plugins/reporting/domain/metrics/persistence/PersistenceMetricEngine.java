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
package io.vertigo.studio.plugins.reporting.domain.metrics.persistence;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.reporting.Metric;
import io.vertigo.studio.reporting.MetricBuilder;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Vérifier si le DT est persistant.
 *
 * @author pchretien
 */
public final class PersistenceMetricEngine implements MetricEngine<DtDefinition> {
	private final StoreManager storeManager;

	/**
	 * Constructeur.
	 * @param storeManager Manager de persistance
	 */
	public PersistenceMetricEngine(final StoreManager storeManager) {
		Assertion.checkNotNull(storeManager);
		//-----
		this.storeManager = storeManager;
	}

	/** {@inheritDoc} */
	@Override
	public Metric execute(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		final boolean test = test(dtDefinition);
		final Metric.Status status;
		if (test) {
			status = Metric.Status.Executed;
		} else {
			status = Metric.Status.Error;
		}

		return new MetricBuilder()
				.withTitle("Persistance")
				.withStatus(status)
				.withValue(dtDefinition.isPersistent())
				.build();
	}

	//On teste si la définition est persistante, elle existe en BDD et le mapping est ok.
	private boolean test(final DtDefinition dtDefinition) {
		if (!dtDefinition.isPersistent()) {
			return true;
		}
		try {
			storeManager.getDataStore().getList(new DtListURIForCriteria<>(dtDefinition, null, 1));
			return true;
		} catch (final Exception e) {
			return false;
		}
	}
}
