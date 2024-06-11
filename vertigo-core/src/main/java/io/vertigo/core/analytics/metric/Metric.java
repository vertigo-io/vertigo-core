/*
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
 * A Metric represents a measurement or observation at a specific point in time.
 *
 * @author mlaroche, pchretien
 */
public record Metric(
		Instant measureInstant,
		String name,
		String module, // may be null for now
		String feature,
		Double value, // might be null
		Status status) {

	/**
	 * The status of a metric can be either SUCCESS or ERROR.
	 */
	public enum Status {
		/**
		 * The status is a success.
		 * example : a mail successfully sent
		 */
		SUCCESS,
		/**
		 * The status is an error.
		 * example : a mail failed to be sent
		 */
		ERROR
	}

	/**
	 * Constructor for Metric.
	 *
	 * @param measureInstant The instant at which the metric was measured.
	 * @param name The name of the metric.
	 * @param module The module associated with the metric. May be null.
	 * @param feature The feature associated with the metric.
	 * @param value The value of the metric. May be null.
	 * @param status The status of the metric.
	 */
	public Metric {
		Assertion.check()
				.isNotNull(measureInstant)
				.isNotBlank(name)
				.isNotBlank(feature)
				.isNotNull(status);
	}

	/**
	 * Static method factory for creating a MetricBuilder.
	 *
	 * @return A new MetricBuilder instance.
	 */
	public static MetricBuilder builder() {
		return new MetricBuilder();
	}
}
