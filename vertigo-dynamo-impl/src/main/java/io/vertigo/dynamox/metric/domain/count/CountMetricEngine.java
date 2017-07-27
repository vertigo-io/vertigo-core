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
package io.vertigo.dynamox.metric.domain.count;

import io.vertigo.commons.impl.metric.MetricEngine;
import io.vertigo.commons.metric.Metric;
import io.vertigo.commons.metric.MetricBuilder;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;

/**
 * Comptage du nombre de lignes.
 *
 * @author pchretien
 */
public final class CountMetricEngine implements MetricEngine<DtDefinition> {
	private final StoreManager storeManager;

	/**
	 * Constructeur.
	 * @param storeManager Manager de persistance
	 */
	public CountMetricEngine(
			final StoreManager storeManager) {
		Assertion.checkNotNull(storeManager);
		//-----
		this.storeManager = storeManager;
	}

	@Override
	public boolean isApplicable(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---
		return dtDefinition.isPersistent();
	}

	/** {@inheritDoc} */
	@Override
	public Metric execute(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkState(dtDefinition.isPersistent(), "Count can only be performed on persistent entities, DtDefinition '{0}' is not", dtDefinition.getName());
		//-----
		final MetricBuilder metricBuilder = Metric.builder()
				.withType("entityCount")
				.withSubject(dtDefinition.getName());
		try {
			final double count = storeManager.getDataStore().count(dtDefinition);
			return metricBuilder
					.withSuccess()
					.withValue(count)
					.build();
		} catch (final Exception e) {
			return metricBuilder
					.withError()
					.build();
		}
	}
}
