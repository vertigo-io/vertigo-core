/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.analytics.metric;

import java.time.Instant;

import io.vertigo.core.lang.Assertion;

/**
 * Metric.
 *
 * @author mlaroche, pchretien
 */
public record Metric(
		Instant measureInstant,
		String name,
		String module, // may be null for now
		String feature,
		Double value, //migth be null
		Status status) {

	public enum Status {
		/** Exécution OK*/
		SUCCESS,
		/** Erreur lors de l'exécution*/
		ERROR
	}

	public Metric {
		Assertion.check()
				.isNotNull(measureInstant)
				.isNotBlank(name)
				.isNotBlank(feature)
				.isNotNull(status);
	}

	/**
	 * Static method factory for ReportMetricBuilder
	 * @return ReportMetricBuilder
	 */
	public static MetricBuilder builder() {
		return new MetricBuilder();
	}
}
