/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.database.timeseries;

import java.io.Serializable;

import io.vertigo.lang.Assertion;

/**
 * @author mlaroche
 *
 */
public final class DataFilter implements Serializable {

	private static final long serialVersionUID = 8368099477041555767L;

	private final String measurement;
	private final String location;
	private final String name;
	private final String module;
	private final String feature;
	private final String additionalWhereClause;// may be null

	DataFilter(
			final String measurement,
			final String location,
			final String name,
			final String module,
			final String feature,
			final String additionalWhereClause) {
		Assertion.checkNotNull(location);
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(module);
		Assertion.checkNotNull(feature);
		//---
		this.measurement = measurement;
		this.location = location;
		this.name = name;
		this.module = module;
		this.feature = feature;
		this.additionalWhereClause = additionalWhereClause;
	}

	public static DataFilterBuilder builder(final String measurement) {
		return new DataFilterBuilder(measurement);
	}

	public String getMeasurement() {
		return measurement;
	}

	public String getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public String getModule() {
		return module;
	}

	public String getFeature() {
		return feature;
	}

	public String getAdditionalWhereClause() {
		return additionalWhereClause;
	}

}
