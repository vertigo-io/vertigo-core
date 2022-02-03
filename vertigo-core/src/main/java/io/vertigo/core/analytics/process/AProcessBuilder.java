/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2022, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.analytics.process;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;

/**
 * This builder allows  to build a process in a fluent way.
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcessBuilder.java,v 1.18 2012/11/08 17:06:27 pchretien Exp $
 */
public final class AProcessBuilder implements Builder<AProcess> {
	private final String myCategory;
	private final Instant start;
	private Instant myEnd;

	private final String myName;

	private final Map<String, Double> measures = new HashMap<>();
	private final Map<String, String> tags = new HashMap<>();

	private final List<AProcess> subProcesses = new ArrayList<>();

	/**
	 * Constructor.
	 * La duree du processus sera obtenue lors de l'appel a la methode build().
	 * @param category category of the processus
	 * @param name name of the process, used for agregation
	 */
	AProcessBuilder(final String category, final String name) {
		Assertion.check()
				.isNotBlank(category, "the process category is required")
				.isNotBlank(name, "the process name is required");
		//---
		myCategory = category;
		myName = name;

		start = Instant.now();
	}

	/**
	 * Constructor.
	 * La duree du processus sera obtenue lors de l'appel a la methode build().
	 * @param category category of the processus
	 * @param name name of the process, used for agregation
	 * @param start beginning of the process
	 * @param end end of the process
	 */
	AProcessBuilder(final String category, final String name, final Instant start, final Instant end) {
		Assertion.check()
				.isNotBlank(category, "the process category is required")
				.isNotBlank(name, "the process name is required")
				.isNotNull(start, "the process start is required")
				.isNotNull(end, "the process end is required");
		//---
		myCategory = category;
		myName = name;

		this.start = start;
		myEnd = end;
	}

	/**
	 * Increments a measure.
	 * if the measure is new,  it's automatically created with the value.
	 * @param name the measure name
	 * @param value  the measure value to increment
	 * @return this builder
	 */
	public AProcessBuilder incMeasure(final String name, final double value) {
		Assertion.check().isNotNull(name, "Measure name is required");
		//---------------------------------------------------------------------
		final Double lastmValue = measures.get(name);
		measures.put(name, lastmValue == null ? value : value + lastmValue);
		return this;
	}

	/**
	 * Upserts a mesaure defined by a name and a value.
	 * @param name  the measure name
	 * @param value  the value measure
	 * @return this builder
	 */
	public AProcessBuilder setMeasure(final String name, final double value) {
		Assertion.check().isNotNull(name, "measure name is required");
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
	public AProcessBuilder addTag(final String name, final String value) {
		Assertion.check()
				.isNotNull(name, "tag name is required")
				.isNotNull(value, "tag value is required");
		//---------------------------------------------------------------------
		tags.put(name, value);
		return this;
	}

	/**
	 * adds a sub process d'un sous processus.
	 * @param subProcess the sub process to add
	 * @return this builder
	 */
	public AProcessBuilder addSubProcess(final AProcess subProcess) {
		Assertion.check().isNotNull(subProcess, "sub process is required ");
		//---------------------------------------------------------------------
		subProcesses.add(subProcess);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AProcess build() {
		final Instant end = myEnd != null ? myEnd : Instant.now();
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
