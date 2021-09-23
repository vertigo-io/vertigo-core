/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2021, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.analytics.trace;

/**
 * This interface defines a collect tracer.
 * This tracer must be used to collect data traces.
 *
 * @author pchretien, npiedeloup
 */
public interface AnalyticsTracer {
	/**
	 * Increments a measure (creates if not exists).
	 * @param name the measure name
	 * @param value the measure increment
	 * @return this tracer
	 */
	AnalyticsTracer incMeasure(final String name, final double value);

	/**
	* Sets a value to the measure. (cleans if exists)
	*
	* You should use it when you have an exception. so you define explicitly one single value.
	* @param name the name of the measure
	* @param value the value of the measure
	 * @return this tracer
	*/
	AnalyticsTracer setMeasure(final String name, final double value);

	/**
	 * Sets a value to a specific tag. (cleans if exists)
	 *
	 * @param name the name of the tag
	 * @param value the value of the tag
	 * @return this tracer
	 */
	AnalyticsTracer setTag(final String name, final String value);
}
