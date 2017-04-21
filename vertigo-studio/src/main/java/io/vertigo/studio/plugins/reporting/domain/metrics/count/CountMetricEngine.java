/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.reporting.ReportMetricEngine;
import io.vertigo.studio.reporting.ReportMetric;
import io.vertigo.studio.reporting.ReportMetricBuilder;

/**
 * Comptage du nombre de lignes.
 *
 * @author pchretien
 */
public final class CountMetricEngine implements ReportMetricEngine<DtDefinition> {
	private final StoreManager storeManager;

	/**
	 * Constructeur.
	 * @param storeManager Manager de persistance
	 */
	public CountMetricEngine(final StoreManager storeManager) {
		Assertion.checkNotNull(storeManager);
		//-----
		this.storeManager = storeManager;
	}

	/** {@inheritDoc} */
	@Override
	public ReportMetric execute(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		final ReportMetricBuilder metricBuilder = ReportMetric.builder()
				.withTitle("Nbre lignes")
				.withUnit("rows");

		if (!dtDefinition.isPersistent()) {
			return metricBuilder
					.withStatus(ReportMetric.Status.REJECTETD)
					.build();
		}
		//Dans le cas ou DT est persistant on compte le nombre de lignes.
		try {
			final int count = storeManager.getDataStore().count(dtDefinition);
			return metricBuilder
					.withStatus(ReportMetric.Status.EXECUTED)
					.withValue(count)
					.build();
		} catch (final Exception e) {
			return metricBuilder
					.withStatus(ReportMetric.Status.ERROR)
					.build();
		}
	}
}
