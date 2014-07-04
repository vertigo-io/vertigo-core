package io.vertigo.studio.plugins.reporting.task;

import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.plugins.reporting.task.metrics.explainplan.ExplainPlanMetricEngine;
import io.vertigo.studio.plugins.reporting.task.metrics.join.JoinMetricEngine;
import io.vertigo.studio.plugins.reporting.task.metrics.performance.PerformanceMetricEngine;
import io.vertigo.studio.plugins.reporting.task.metrics.requestsize.RequestSizeMetricEngine;
import io.vertigo.studio.plugins.reporting.task.metrics.subrequest.SubRequestMetricEngine;
import io.vertigo.studio.reporting.DataReport;
import io.vertigo.studio.reporting.Metric;
import io.vertigo.studio.reporting.MetricEngine;
import io.vertigo.studio.reporting.Report;
import io.vertigo.studio.reporting.ReportingPlugin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Implémentation de TaskReportingManager.
 * 
 * @author tchassagnette
 */
public final class TaskReportingPlugin implements ReportingPlugin {
	private final KTransactionManager transactionManager;
	private final TaskManager taskManager;
	private final List<MetricEngine<TaskDefinition, ? extends Metric>> metricEngines;

	@Inject
	public TaskReportingPlugin(final KTransactionManager transactionManager, final TaskManager taskManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(taskManager);
		//---------------------------------------------------------------------
		this.transactionManager = transactionManager;
		this.taskManager = taskManager;
		metricEngines = createMetricEngines();

	}

	/** {@inheritDoc} */
	public Report analyze() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return doAnalyze();
		}

	}

	private Report doAnalyze() {
		final List<DataReport> taskAnalyseResults = new ArrayList<>();
		for (final TaskDefinition taskDefinition : Home.getDefinitionSpace().getAll(TaskDefinition.class)) {
			final List<Metric> results = new ArrayList<>();
			for (final MetricEngine<TaskDefinition, ? extends Metric> metricEngine : metricEngines) {
				final Metric result = metricEngine.execute(taskDefinition);
				results.add(result);
			}
			final TaskDefinitionReport result = new TaskDefinitionReport(taskDefinition, results);
			taskAnalyseResults.add(result);
		}
		return new Report(taskAnalyseResults);
	}

	private List<MetricEngine<TaskDefinition, ? extends Metric>> createMetricEngines() {
		final List<MetricEngine<TaskDefinition, ? extends Metric>> tmpMmetricEngines = new ArrayList<>();
		tmpMmetricEngines.add(new PerformanceMetricEngine(taskManager));
		tmpMmetricEngines.add(new RequestSizeMetricEngine());
		tmpMmetricEngines.add(new ExplainPlanMetricEngine(taskManager));
		tmpMmetricEngines.add(new JoinMetricEngine());
		tmpMmetricEngines.add(new SubRequestMetricEngine());
		return tmpMmetricEngines;
	}
}
