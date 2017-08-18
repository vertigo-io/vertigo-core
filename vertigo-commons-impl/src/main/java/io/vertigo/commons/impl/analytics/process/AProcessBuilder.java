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
package io.vertigo.commons.impl.analytics.process;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This builder allows  to build a process in a fluent way.
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcessBuilder.java,v 1.18 2012/11/08 17:06:27 pchretien Exp $
 */
final class AProcessBuilder implements Builder<AProcess> {
	private final String myCategory;
	private final Instant start;

	private final String myName;

	private final Map<String, Double> measures;
	private final Map<String, String> tags;

	private final List<AProcess> subProcesses;

	/**
	 * Constructor.
	 * La duree du processus sera obtenue lors de l'appel a la methode build().
	 * @param type Type du processus
	 */
	AProcessBuilder(final String category, final String name) {
		Assertion.checkArgNotEmpty(category, "the process category is required");
		Assertion.checkArgNotEmpty(name, "the process name is required");
		//---
		myCategory = category;
		myName = name;

		measures = new HashMap<>();
		tags = new HashMap<>();
		subProcesses = new ArrayList<>();
		start = Instant.now();
	}

	/**
	 * Increments a measure.
	 * if the measure is new,  it's automatically created with the value.
	 * @param name the measure name
	 * @param value  the measure value to increment
	 * @return this builder
	 */
	AProcessBuilder incMeasure(final String measureName, final double measureValue) {
		Assertion.checkNotNull(measureName, "Measure name is required");
		//---------------------------------------------------------------------
		final Double lastmValue = measures.get(measureName);
		measures.put(measureName, lastmValue == null ? measureValue : measureValue + lastmValue);
		return this;
	}

	/**
	 * Upserts a mesaure defined by a name and a value.
	 * @param name  the measure name
	 * @param value  the value measure
	 * @return this builder
	 */
	AProcessBuilder setMeasure(final String name, final double value) {
		Assertion.checkNotNull(name, "measure name is required");
		//---------------------------------------------------------------------
		measures.put(name, value);
		return this;
	}

	/**
	 * Adds a tag defined by a name and a value.
	 * @param name the tag name
	 * @param value  the tag value
	 * @return this builder
	 */
	AProcessBuilder addTag(final String name, final String value) {
		Assertion.checkNotNull(name, "tag name is required");
		Assertion.checkNotNull(value, "tag value is required");
		//---------------------------------------------------------------------
		tags.put(name, value);
		return this;
	}

	/**
	 * adds a sub process d'un sous processus.
	 * @param subProcess the sub process to add
	 * @return this builder
	 */
	AProcessBuilder addSubProcess(final AProcess subProcess) {
		Assertion.checkNotNull(subProcess, "sub process is required ");
		//---------------------------------------------------------------------
		subProcesses.add(subProcess);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AProcess build() {
		final Instant end = Instant.now();
		return new AProcess(
				myCategory,
				myName,
				start,
				end,
				measures,
				tags,
				subProcesses);
	}
}
