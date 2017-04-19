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
package io.vertigo.studio.reporting;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.studio.reporting.ReportMetric.Status;

/**
 * Builder de metric.
 *
 * @author pchretien
 */
public final class ReportMetricBuilder implements Builder<ReportMetric> {
	private Status myStatus = Status.EXECUTED; //default not required
	private String myTitle; //require
	private String myUnit = ""; //default not required
	private Object myValue; //null by default not required
	private String myValueInformation; //null by default not required

	@Override
	public ReportMetric build() {
		return new ReportMetric(myStatus, myTitle, myUnit, myValue, myValueInformation);
	}

	/**
	 * Status de la métrique.
	 * @return Builder
	 */
	public ReportMetricBuilder withStatus(final Status status) {
		Assertion.checkNotNull(status);
		//-----
		myStatus = status;
		return this;
	}

	/**
	 * Titre de la métrique. (notNull)
	 * @return Builder
	 */
	public ReportMetricBuilder withTitle(final String title) {
		Assertion.checkArgNotEmpty(title);
		//-----
		myTitle = title;
		return this;
	}

	/**
	 * Unité de la métrique. (notNull)
	 * @return Builder
	 */
	public ReportMetricBuilder withUnit(final String unit) {
		Assertion.checkArgNotEmpty(unit);
		//-----
		myUnit = unit;
		return this;
	}

	/**
	 * Valeur de la métrique. (Integer, Long, String, etc..)
	 * @return Builder
	 */
	public ReportMetricBuilder withValue(final Object value) {
		myValue = value;
		return this;
	}

	/**
	 * Complément d'information sur la valeur. (nullable)
	 * @return Builder
	 */
	public ReportMetricBuilder withValueInformation(final String valueInformation) {
		myValueInformation = valueInformation;
		return this;
	}
}
