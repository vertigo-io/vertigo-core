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
package io.vertigo.studio.plugins.reporting.task.metrics.subrequest;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.reporting.Metric;
import io.vertigo.studio.reporting.MetricBuilder;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Plugin qui compte le nombre de ss requete dans une requete SQL.
 *
 * @author tchassagnette
 */
public final class SubRequestMetricEngine implements MetricEngine<TaskDefinition> {
	/** {@inheritDoc} */
	@Override
	public Metric execute(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//-----
		final int subRequestCount = taskDefinition.getRequest().toUpperCase().split("SELECT").length - 1;
		return new MetricBuilder()
				.withTitle("Nombre de ss-requêtes")
				.withValue(subRequestCount)
				.build();
	}
}
