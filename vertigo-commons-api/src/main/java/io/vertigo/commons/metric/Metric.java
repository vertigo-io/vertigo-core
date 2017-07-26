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

import io.vertigo.lang.Assertion;

/**
 * Interface décrivant un résultat de metric.
 *
 * @author tchassagnette, pchretien
 */
public final class Metric {
	public enum Status {
		/** Exécution OK*/
		EXECUTED,
		/** Erreur lors de l'exécution*/
		ERROR,
		/** Métrique non pertinente*/
		REJECTETD
	}

	private final Status status;
	private final String title;
	private final String unit;
	private final Object value;
	private final String valueInformation;

	Metric(final Status status, final String title, final String unit, final Object value, final String valueInformation) {
		Assertion.checkNotNull(status);
		Assertion.checkArgNotEmpty(title);
		Assertion.checkNotNull(unit); //may be empty
		//-----
		this.status = status;
		this.title = title;
		this.unit = unit;
		this.value = value;
		this.valueInformation = valueInformation;
	}

	/**
	 * Static method factory for ReportMetricBuilder
	 * @return ReportMetricBuilder
	 */
	public static MetricBuilder builder() {
		return new MetricBuilder();
	}

	/**
	 * @return Status de la métrique.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return Titre de la métrique. (notNull)
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Unité de la métrique. (notNull)
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * @return Valeur de la métrique. (Integer, Long, String, etc..)
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return Complément d'information sur la valeur. (nullable)
	 */
	public String getValueInformation() {
		return valueInformation;
	}
}
