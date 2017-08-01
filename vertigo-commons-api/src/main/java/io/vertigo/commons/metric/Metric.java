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
package io.vertigo.commons.metric;

import java.time.Instant;

import io.vertigo.lang.Assertion;

/**
 * Interface décrivant un résultat de metric.
 *
 * @author mlaroche, pchretien
 */
public final class Metric {

	public enum Status {
		/** Exécution OK*/
		SUCCESS,
		/** Erreur lors de l'exécution*/
		ERROR;
	}

	private final Instant measureInstant;
	private final String type;
	private final String subject;
	private final Double value;//migth be null
	private final Status status;

	Metric(
			final Instant measureTime,
			final String type,
			final String subject,
			final Double value,
			final Status status) {
		Assertion.checkNotNull(measureTime);
		Assertion.checkArgNotEmpty(type);
		Assertion.checkArgNotEmpty(subject);
		Assertion.checkNotNull(status);
		//-----
		this.measureInstant = measureTime;
		this.type = type;
		this.subject = subject;
		this.value = value;
		this.status = status;

	}

	/**
	 * Static method factory for ReportMetricBuilder
	 * @return ReportMetricBuilder
	 */
	public static MetricBuilder builder() {
		return new MetricBuilder();
	}

	public Instant getMeasureInstant() {
		return measureInstant;
	}

	public String getType() {
		return type;
	}

	public String getSubject() {
		return subject;
	}

	public Double getValue() {
		return value;
	}

	public Status getStatus() {
		return status;
	}

}
