/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.analytics.health;

import java.time.Instant;

import io.vertigo.core.lang.Assertion;

/**
 * Represents a single health check measurement in the system.
 * Provides structured information about component health status.
 * Format: {plugin/component}.{test}
 * 
 * Example: dataStorePlugin.ping - Database connectivity check
 *
 * @author mlaroche
 * 
 * @param name Health check identifier
 * @param checker Component that performed the check
 * @param module Technical or functional module (e.g., commons, administration)
 * @param feature Semantic grouping of related checks (e.g., database, billing)
 * @param checkInstant Timestamp of check execution
 * @param healthMeasure Actual health measurement result
 */
public record HealthCheck(
		String name,
		String checker,
		String module,
		String feature,
		Instant checkInstant,
		HealthMeasure healthMeasure) {

	public HealthCheck {
		Assertion.check()
				.isNotBlank(name)
				.isNotBlank(checker)
				.isNotBlank(module)
				.isNotBlank(feature)
				.isNotNull(checkInstant)
				.isNotNull(healthMeasure);
	}
}
