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
package io.vertigo.commons.analytics;

/**
 * This interface defines a collect tracker.
 * This tracker must be used to collect data during a process.
 *
 * @author pchretien, npiedeloup
 */
public interface AnalyticsTracker extends AutoCloseable {

	/**
	 * Increments a measure (creates if not exists).
	 * @param measureType the type of the measure
	 * @param value the increment of the measure
	 * @return this tracker
	 */
	AnalyticsTracker incMeasure(final String measureType, final double value);

	/**
	* Sets a value to the measure. (cleans if exists)
	* You should use it when you have an exception. so you define explicitly one single value.
	* @param measureType the type of the measure
	* @param value the value of the measure
	 * @return this tracker
	*/
	AnalyticsTracker setMeasure(final String measureType, final double value);

	/**
	 * Sets a value to a specific metadata. (cleans if exists)
	 *
	 * @param metaDataName the name of the metadata
	 * @param value the value of the metadataValeur de la meta-donnée
	 * @return this tracker
	 */
	AnalyticsTracker addMetaData(final String metaDataName, final String value);

	/**
	 * Marks this tracker as succeeded.
	 * @return this tracker
	 */
	AnalyticsTracker markAsSucceeded();

	/** {@inheritDoc} */
	@Override
	void close();
}
