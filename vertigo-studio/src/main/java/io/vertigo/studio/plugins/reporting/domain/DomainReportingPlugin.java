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
package io.vertigo.studio.plugins.reporting.domain;

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.reporting.domain.metrics.count.CountMetricEngine;
import io.vertigo.studio.plugins.reporting.domain.metrics.dependency.DependencyMetricEngine;
import io.vertigo.studio.plugins.reporting.domain.metrics.fields.FieldsMetricEngine;
import io.vertigo.studio.plugins.reporting.domain.metrics.persistence.PersistenceMetricEngine;
import io.vertigo.studio.reporting.DataReport;
import io.vertigo.studio.reporting.Metric;
import io.vertigo.studio.reporting.MetricEngine;
import io.vertigo.studio.reporting.Report;
import io.vertigo.studio.reporting.ReportingPlugin;
import io.vertigo.util.ListBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Implémentation de ReportingPlugin.
 *
 * @author pchretien
 */
public final class DomainReportingPlugin implements ReportingPlugin {
	private final KTransactionManager transactionManager;
	private final PersistenceManager persistenceManager;
	private final List<MetricEngine<DtDefinition, ? extends Metric>> metricEngines;

	@Inject
	public DomainReportingPlugin(final KTransactionManager transactionManager, final PersistenceManager persistenceManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(persistenceManager);
		//---------------------------------------------------------------------
		this.transactionManager = transactionManager;
		this.persistenceManager = persistenceManager;
		metricEngines = createMetricEngines();

	}

	/** {@inheritDoc} */
	public Report analyze() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return doAnalyze();
		}
	}

	private Report doAnalyze() {
		final List<DataReport> domainAnalysisList = new ArrayList<>();
		for (final DtDefinition dtDefinition : Home.getDefinitionSpace().getAll(DtDefinition.class)) {
			final List<Metric> results = new ArrayList<>();
			for (final MetricEngine<DtDefinition, ? extends Metric> metricEngine : metricEngines) {
				final Metric result = metricEngine.execute(dtDefinition);
				results.add(result);
			}
			final DataReport result = new DtDefinitionReport(dtDefinition, results);
			domainAnalysisList.add(result);
		}
		return new Report(domainAnalysisList);
	}

	private List<MetricEngine<DtDefinition, ? extends Metric>> createMetricEngines() {
		return new ListBuilder<MetricEngine<DtDefinition, ? extends Metric>>()
				.add(new FieldsMetricEngine())
				.add(new DependencyMetricEngine())
				.add(new PersistenceMetricEngine(persistenceManager))
				.add(new CountMetricEngine(persistenceManager))
				.unmodifiable()
				.build();
	}
}
