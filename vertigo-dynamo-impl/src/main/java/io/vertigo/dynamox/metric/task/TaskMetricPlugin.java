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
package io.vertigo.dynamox.metric.task;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.metric.Metric;
import io.vertigo.commons.metric.MetricEngine;
import io.vertigo.commons.metric.MetricPlugin;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamox.metric.task.join.JoinMetricEngine;
import io.vertigo.dynamox.metric.task.performance.PerformanceMetricEngine;
import io.vertigo.dynamox.metric.task.requestsize.RequestSizeMetricEngine;
import io.vertigo.dynamox.metric.task.subrequest.SubRequestMetricEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;

/**
 * Implémentation de TaskReportingManager.
 *
 * @author tchassagnette
 */
public final class TaskMetricPlugin implements MetricPlugin {

	private final List<MetricEngine<TaskDefinition>> metricEngines;

	@Inject
	public TaskMetricPlugin(final VTransactionManager transactionManager, final TaskManager taskManager, final SqlDataBaseManager sqlDataBaseManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(taskManager);
		Assertion.checkNotNull(sqlDataBaseManager);
		//-----
		metricEngines = new ListBuilder<MetricEngine<TaskDefinition>>()
				.add(new PerformanceMetricEngine(transactionManager, taskManager))
				.add(new RequestSizeMetricEngine())
				.add(new JoinMetricEngine())
				.add(new SubRequestMetricEngine())
				.unmodifiable()
				.build();

	}

	/** {@inheritDoc} */
	@Override
	public List<Metric> analyze() {
		final Collection<TaskDefinition> taskDefinitions = Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class);

		return metricEngines
				.stream()
				.flatMap(engine -> taskDefinitions
						.stream()
						.filter(taskDefinition -> engine.isApplicable(taskDefinition))
						.map(taskDefinition -> engine.execute(taskDefinition))
						.collect(Collectors.toList())
						.stream())
				.collect(Collectors.toList());

	}

}
