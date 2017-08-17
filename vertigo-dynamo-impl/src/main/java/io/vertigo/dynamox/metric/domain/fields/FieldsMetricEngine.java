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
package io.vertigo.dynamox.metric.domain.fields;

import io.vertigo.commons.metric.Metric;
import io.vertigo.commons.metric.MetricEngine;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;

/**
 * Comptage du nombre de champs.
 *
 * @author pchretien
 */
public final class FieldsMetricEngine implements MetricEngine<DtDefinition> {
	/** {@inheritDoc} */
	@Override
	public Metric execute(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		final double size = dtDefinition.getFields().size();
		return Metric.builder()
				.withSuccess()
				.withName("definitionFieldCount")
				.withTopic(dtDefinition.getName())
				.withValue(size)
				.build();
	}
}
