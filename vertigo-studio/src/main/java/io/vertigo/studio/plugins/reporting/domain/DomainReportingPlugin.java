package io.vertigo.studio.plugins.reporting.domain;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.plugins.reporting.domain.metrics.count.CountMetricEngine;
import io.vertigo.studio.plugins.reporting.domain.metrics.dependency.DependencyMetricEngine;
import io.vertigo.studio.plugins.reporting.domain.metrics.fields.FieldsMetricEngine;
import io.vertigo.studio.plugins.reporting.domain.metrics.persistence.PersistenceMetricEngine;
import io.vertigo.studio.reporting.DataReport;
import io.vertigo.studio.reporting.Metric;
import io.vertigo.studio.reporting.MetricEngine;
import io.vertigo.studio.reporting.Report;
import io.vertigo.studio.reporting.ReportingPlugin;

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
		final List<MetricEngine<DtDefinition, ? extends Metric>> tmpMmetricEngines = new ArrayList<>();
		tmpMmetricEngines.add(new FieldsMetricEngine());
		tmpMmetricEngines.add(new DependencyMetricEngine());
		tmpMmetricEngines.add(new PersistenceMetricEngine(persistenceManager));
		tmpMmetricEngines.add(new CountMetricEngine(persistenceManager));
		return tmpMmetricEngines;
	}
}
