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
package io.vertigo.studio.plugins.reporting.domain.metrics.dependency;

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.reporting.Metric;
import io.vertigo.studio.reporting.MetricBuilder;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Comptage du nombre de champs.
 *
 * @author pchretien
 */
public final class DependencyMetricEngine implements MetricEngine<DtDefinition> {

	/** {@inheritDoc} */
	@Override
	public Metric execute(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		final int count = count(dtDefinition);
		return new MetricBuilder()
				.withTitle("Utilisation dans les dao")
				.withValue(count)
				.build();
	}

	private static int count(final DtDefinition dtDefinition) {
		int count = 0;
		for (final TaskDefinition taskDefinition : Home.getApp().getDefinitionSpace().getAll(TaskDefinition.class)) {
			for (final TaskAttribute taskAttribute : taskDefinition.getInAttributes()) {
				count += count(dtDefinition, taskAttribute);
			}
			if (taskDefinition.getOutAttributeOption().isDefined()) {
				final TaskAttribute taskAttribute = taskDefinition.getOutAttributeOption().get();
				count += count(dtDefinition, taskAttribute);
			}
		}
		return count;
	}

	private static int count(final DtDefinition dtDefinition, final TaskAttribute taskAttribute) {
		int count = 0;
		if (!taskAttribute.getDomain().getDataType().isPrimitive()) {
			if (taskAttribute.getDomain().hasDtDefinition()) {
				if (dtDefinition.equals(taskAttribute.getDomain().getDtDefinition())) {
					count++;
				}
			}
		}
		return count;
	}
}
