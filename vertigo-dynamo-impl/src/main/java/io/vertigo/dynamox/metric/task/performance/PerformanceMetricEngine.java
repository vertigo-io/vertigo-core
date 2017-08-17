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
package io.vertigo.dynamox.metric.task.performance;

import io.vertigo.commons.metric.Metric;
import io.vertigo.commons.metric.MetricBuilder;
import io.vertigo.commons.metric.MetricEngine;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;

/**
 * Plugin de calcul du temps d'exécution d'une requête.
 *
 * @author tchassagnette
 */
public final class PerformanceMetricEngine implements MetricEngine<TaskDefinition> {

	private final VTransactionManager transactionManager;
	private final TaskManager taskManager;

	/**
	 * Constructeur apr défaut.
	 * @param transactionManager Transaction Manager
	 * @param taskManager Manager des tasks
	 */
	public PerformanceMetricEngine(
			final VTransactionManager transactionManager,
			final TaskManager taskManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(taskManager);
		//-----
		this.transactionManager = transactionManager;
		this.taskManager = taskManager;
	}

	/** {@inheritDoc} */
	@Override
	public Metric execute(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//-----
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return doExecute(taskDefinition);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//---
		return TaskEngineSelect.class.isAssignableFrom(taskDefinition.getTaskEngineClass()) && !hasNotNullOutParams(taskDefinition);
	}

	private Metric doExecute(final TaskDefinition taskDefinition) {
		final MetricBuilder metricBuilder = Metric.builder()
				.withName("taskExecutionTime")
				.withTopic(taskDefinition.getName());

		try {
			final TaskPopulator taskPopulator = new TaskPopulator(taskDefinition);
			final Task task = taskPopulator.populateTask();
			final double startTime = System.currentTimeMillis();
			taskManager.execute(task);
			final double endTime = System.currentTimeMillis();
			final double executionTime = endTime - startTime;
			return metricBuilder
					.withSuccess()
					.withValue(executionTime)
					.build();
		} catch (final Exception e) {
			return metricBuilder
					.withError()
					.build();
		}
	}

	private static boolean hasNotNullOutParams(final TaskDefinition taskDefinition) {
		return taskDefinition.getOutAttributeOption().isPresent()
				&& taskDefinition.getOutAttributeOption().get().isRequired();
	}
}
