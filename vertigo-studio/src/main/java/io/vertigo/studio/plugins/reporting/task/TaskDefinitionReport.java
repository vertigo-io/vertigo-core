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

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.reporting.DataReport;
import io.vertigo.studio.reporting.Metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Résultat d'analyse d'une tâche.
 *
 * @author tchassagnette
 */
public final class TaskDefinitionReport implements DataReport {
	private final TaskDefinition taskDefinition;
	private final List<Metric> metrics;

	/**
	 * Constructeur par défaut.
	 * @param taskDefinition Définition de la tâche.
	 */
	TaskDefinitionReport(final TaskDefinition taskDefinition, final List<Metric> metrics) {
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(metrics);
		//-----
		this.taskDefinition = taskDefinition;
		this.metrics = new ArrayList<>(metrics);
	}

	/** {@inheritDoc} */
	@Override
	public List<Metric> getMetrics() {
		return Collections.unmodifiableList(metrics);
	}

	/** {@inheritDoc} */
	@Override
	public String getTitle() {
		return taskDefinition.getName();
	}

	/** {@inheritDoc} */
	@Override
	public String getFileName() {
		return taskDefinition.getName() + ".html";
	}

	/** {@inheritDoc} */
	@Override
	public String getHtmlDescription() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<h1>").append(taskDefinition.getName()).append("</h1>");
		sb.append("<h2>TaskEngine</h2>");
		sb.append(taskDefinition.getTaskEngineClass().getName());
		sb.append("<h2>Requête</h2>");
		sb.append(taskDefinition.getRequest().replaceAll("\n", "<br/>"));
		sb.append("<h2>Exécution des plugins</h2>");
		return sb.toString();
	}
}
