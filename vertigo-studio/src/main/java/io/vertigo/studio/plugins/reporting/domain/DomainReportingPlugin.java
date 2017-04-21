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
package io.vertigo.studio.plugins.reporting.domain;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.reporting.ReportMetricEngine;
import io.vertigo.studio.impl.reporting.ReportingPlugin;
import io.vertigo.studio.plugins.reporting.domain.metrics.count.CountMetricEngine;
import io.vertigo.studio.plugins.reporting.domain.metrics.dependency.DependencyMetricEngine;
import io.vertigo.studio.plugins.reporting.domain.metrics.fields.FieldsMetricEngine;
import io.vertigo.studio.plugins.reporting.domain.metrics.persistence.PersistenceMetricEngine;
import io.vertigo.studio.reporting.Report;
import io.vertigo.studio.reporting.ReportLine;
import io.vertigo.studio.reporting.ReportMetric;
import io.vertigo.util.ListBuilder;

/**
 * Implémentation de ReportingPlugin.
 *
 * @author pchretien
 */
public final class DomainReportingPlugin implements ReportingPlugin {
	private final VTransactionManager transactionManager;
	private final StoreManager storeManager;
	private final List<ReportMetricEngine<DtDefinition>> metricEngines;

	/**
	 * Constructor.
	 * @param transactionManager the transactionManager
	 * @param storeManager the storeManager
	 */
	@Inject
	public DomainReportingPlugin(final VTransactionManager transactionManager, final StoreManager storeManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(storeManager);
		//-----
		this.transactionManager = transactionManager;
		this.storeManager = storeManager;
		metricEngines = createMetricEngines();

	}

	/** {@inheritDoc} */
	@Override
	public Report analyze() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return doAnalyze();
		}
	}

	private Report doAnalyze() {
		final List<ReportLine> domainAnalysisList = new ArrayList<>();
		for (final DtDefinition dtDefinition : Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)) {
			final List<ReportMetric> results = new ArrayList<>();
			for (final ReportMetricEngine<DtDefinition> metricEngine : metricEngines) {
				final ReportMetric result = metricEngine.execute(dtDefinition);
				results.add(result);
			}
			final ReportLine result = new DtDefinitionReport(dtDefinition, results);
			domainAnalysisList.add(result);
		}
		return new Report(this.getClass().getSimpleName(), domainAnalysisList);
	}

	private List<ReportMetricEngine<DtDefinition>> createMetricEngines() {
		return new ListBuilder<ReportMetricEngine<DtDefinition>>()
				.add(new FieldsMetricEngine())
				.add(new DependencyMetricEngine())
				.add(new PersistenceMetricEngine(storeManager))
				.add(new CountMetricEngine(storeManager))
				.unmodifiable()
				.build();
	}
}
