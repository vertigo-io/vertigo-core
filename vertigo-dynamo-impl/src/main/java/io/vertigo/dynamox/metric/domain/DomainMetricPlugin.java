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
package io.vertigo.dynamox.metric.domain;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.impl.metric.MetricPlugin;
import io.vertigo.commons.metric.Metric;
import io.vertigo.commons.metric.MetricEngine;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamox.metric.domain.count.CountMetricEngine;
import io.vertigo.dynamox.metric.domain.dependency.DependencyMetricEngine;
import io.vertigo.dynamox.metric.domain.fields.FieldsMetricEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;

/**
 * Implémentation de ReportingPlugin.
 *
 * @author pchretien
 */
public final class DomainMetricPlugin implements MetricPlugin {
	private final VTransactionManager transactionManager;
	private final List<MetricEngine<DtDefinition>> staticMetricEngines;
	private final CountMetricEngine countMetricEngine;

	/**
	 * Constructor.
	 * @param transactionManager the transactionManager
	 * @param storeManager the storeManager
	 */
	@Inject
	public DomainMetricPlugin(final VTransactionManager transactionManager, final StoreManager storeManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(storeManager);
		//-----
		this.transactionManager = transactionManager;
		staticMetricEngines = new ListBuilder<MetricEngine<DtDefinition>>()
				.add(new FieldsMetricEngine())
				.add(new DependencyMetricEngine())
				.unmodifiable()
				.build();

		countMetricEngine = new CountMetricEngine(storeManager);

	}

	/** {@inheritDoc} */
	@Override
	public List<Metric> analyze() {
		final Collection<DtDefinition> dtDefinitions = Home.getApp().getDefinitionSpace().getAll(DtDefinition.class);

		final Stream<Metric> staticMetrics = staticMetricEngines
				.stream()
				.flatMap(engine -> dtDefinitions
						.stream()
						.filter(dtDefinition -> engine.isApplicable(dtDefinition))
						.map(dtDefinition -> engine.execute(dtDefinition))
						.collect(Collectors.toList())
						.stream());

		// for the countMetric engine we use a single transaction
		final List<Metric> countMetrics;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			countMetrics = dtDefinitions
					.stream()
					.filter(dtDefinition -> countMetricEngine.isApplicable(dtDefinition))
					.map(dtDefinition -> doExecute(countMetricEngine, dtDefinition))
					.collect(Collectors.toList());
		}

		return Stream.concat(staticMetrics, countMetrics.stream())
				.collect(Collectors.toList());

	}

	private Metric doExecute(final MetricEngine<DtDefinition> engine, final DtDefinition dtDefinition) {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return engine.execute(dtDefinition);
		}
	}

}
