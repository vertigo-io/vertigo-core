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
package io.vertigo.studio.plugins.reporting.task;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.reporting.ReportMetricEngine;
import io.vertigo.studio.impl.reporting.ReportingPlugin;
import io.vertigo.studio.plugins.reporting.task.metrics.explainplan.ExplainPlanMetricEngine;
import io.vertigo.studio.plugins.reporting.task.metrics.join.JoinMetricEngine;
import io.vertigo.studio.plugins.reporting.task.metrics.performance.PerformanceMetricEngine;
import io.vertigo.studio.plugins.reporting.task.metrics.requestsize.RequestSizeMetricEngine;
import io.vertigo.studio.plugins.reporting.task.metrics.subrequest.SubRequestMetricEngine;
import io.vertigo.studio.reporting.Report;
import io.vertigo.studio.reporting.ReportLine;
import io.vertigo.studio.reporting.ReportMetric;
import io.vertigo.util.ListBuilder;

/**
 * Implémentation de TaskReportingManager.
 *
 * @author tchassagnette
 */
public final class TaskReportingPlugin implements ReportingPlugin {
	private final VTransactionManager transactionManager;
	private final TaskManager taskManager;
	private final SqlDataBaseManager sqlDataBaseManager;

	private final List<ReportMetricEngine<TaskDefinition>> metricEngines;

	@Inject
	public TaskReportingPlugin(final VTransactionManager transactionManager, final TaskManager taskManager, final SqlDataBaseManager sqlDataBaseManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(taskManager);
		Assertion.checkNotNull(sqlDataBaseManager);
		//-----
		this.transactionManager = transactionManager;
		this.taskManager = taskManager;
		this.sqlDataBaseManager = sqlDataBaseManager;
		metricEngines = createMetricEngines();

	}

	/** {@inheritDoc} */
	@Override
	public Report analyze() {
		final List<ReportLine> taskAnalyseResults = new ArrayList<>();
		for (final TaskDefinition taskDefinition : Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)) {
			final List<ReportMetric> results = new ArrayList<>();
			for (final ReportMetricEngine<TaskDefinition> metricEngine : metricEngines) {
				//on crée un transaction à chaque fois, car elle peut-être inutilisable
				try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
					final ReportMetric result = metricEngine.execute(taskDefinition);
					results.add(result);
				}
			}
			final TaskDefinitionReport result = new TaskDefinitionReport(taskDefinition, results);
			taskAnalyseResults.add(result);
		}
		return new Report(this.getClass().getSimpleName(), taskAnalyseResults);
	}

	private List<ReportMetricEngine<TaskDefinition>> createMetricEngines() {
		return new ListBuilder<ReportMetricEngine<TaskDefinition>>()
				.add(new PerformanceMetricEngine(taskManager))
				.add(new RequestSizeMetricEngine())
				.add(new ExplainPlanMetricEngine(taskManager, sqlDataBaseManager))
				.add(new JoinMetricEngine())
				.add(new SubRequestMetricEngine())
				.unmodifiable()
				.build();
	}
}
