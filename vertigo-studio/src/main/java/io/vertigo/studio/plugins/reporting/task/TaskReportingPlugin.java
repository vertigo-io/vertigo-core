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
package io.vertigo.studio.plugins.reporting.task;

import io.vertigo.core.Home;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.lang.Assertion;
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
import io.vertigo.util.ListBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Implémentation de TaskReportingManager.
 *
 * @author tchassagnette
 */
public final class TaskReportingPlugin implements ReportingPlugin {
	private final VTransactionManager transactionManager;
	private final TaskManager taskManager;
	private final List<MetricEngine<TaskDefinition>> metricEngines;

	@Inject
	public TaskReportingPlugin(final VTransactionManager transactionManager, final TaskManager taskManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(taskManager);
		//-----
		this.transactionManager = transactionManager;
		this.taskManager = taskManager;
		metricEngines = createMetricEngines();

	}

	/** {@inheritDoc} */
	@Override
	public Report analyze() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return doAnalyze();
		}

	}

	private Report doAnalyze() {
		final List<DataReport> taskAnalyseResults = new ArrayList<>();
		for (final TaskDefinition taskDefinition : Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)) {
			final List<Metric> results = new ArrayList<>();
			for (final MetricEngine<TaskDefinition> metricEngine : metricEngines) {
				final Metric result = metricEngine.execute(taskDefinition);
				results.add(result);
			}
			final TaskDefinitionReport result = new TaskDefinitionReport(taskDefinition, results);
			taskAnalyseResults.add(result);
		}
		return new Report(taskAnalyseResults);
	}

	private List<MetricEngine<TaskDefinition>> createMetricEngines() {
		return new ListBuilder<MetricEngine<TaskDefinition>>()
				.add(new PerformanceMetricEngine(taskManager))
				.add(new RequestSizeMetricEngine())
				.add(new ExplainPlanMetricEngine(taskManager))
				.add(new JoinMetricEngine())
				.add(new SubRequestMetricEngine())
				.unmodifiable()
				.build();
	}
}
