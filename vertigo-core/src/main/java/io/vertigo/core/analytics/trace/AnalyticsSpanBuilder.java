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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;

/**
 * This builder allows to build a span in a fluent way.
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcessBuilder.java,v 1.18 2012/11/08 17:06:27 pchretien Exp $
 */
public final class AnalyticsSpanBuilder implements Builder<AnalyticsSpan> {
	private final String myCategory;
	private final Instant start;
	private Instant myEnd;

	private final String myName;

	private final Map<String, Double> measures = new HashMap<>();
	private final Map<String, String> tags = new HashMap<>();

	private final List<AnalyticsSpan> childSpans = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * the duration will be computed when the build() method will be called.
	 * @param category  the span category
	 * @param name the span name
	 */
	AnalyticsSpanBuilder(final String category, final String name) {
		Assertion.check()
				.isNotBlank(category, "the span category is required")
				.isNotBlank(name, "the span name is required");
		//---
		myCategory = category;
		myName = name;

		start = Instant.now();
	}

	/**
	 * Constructor.
	 *
	 * @param category the span category
	 * @param name the span name
	 * @param start the span start
	 * @param end the span end
	 */
	AnalyticsSpanBuilder(final String category, final String name, final Instant start, final Instant end) {
		Assertion.check()
				.isNotBlank(category, "the span category is required")
				.isNotBlank(name, "the span name is required")
				.isNotNull(start, "the span start is required")
				.isNotNull(end, "the span end is required");
		//---
		myCategory = category;
		myName = name;

		this.start = start;
		myEnd = end;
	}

	/**
	 * Increments a measure.
	 * if the measure is new,  it's automatically created with the value.
	 *
	 * @param name the measure name
	 * @param value  the measure value to increment
	 * @return this builder
	 */
	public AnalyticsSpanBuilder incMeasure(final String name, final double value) {
		Assertion.check().isNotNull(name, "Measure name is required");
		//---
		final Double lastmValue = measures.get(name);
		measures.put(name, lastmValue == null ? value : value + lastmValue);
		return this;
	}

	/**
	 * Initializes a measure defined by a name and a value.
	 *
	 * @param name  the measure name
	 * @param value  the value measure
	 * @return this builder
	 */
	public AnalyticsSpanBuilder withMeasure(final String name, final double value) {
		Assertion.check()
				.isNotNull(name, "measure name is required");
		//---
		measures.put(name, value);
		return this;
	}

	/**
	 * Sets a tag defined by a name and a value.
	 *
	 * @param name the tag name
	 * @param value the tag value
	 * @return this builder
	 */
	public AnalyticsSpanBuilder withTag(final String name, final String value) {
		Assertion.check()
				.isNotNull(name, "tag name is required")
				.isNotNull(value, "tag value is required");
		//---
		tags.put(name, value);
		return this;
	}

	/**
	 * Adds a child span.
	 *
	 * @param childSpan the child span to add
	 * @return this builder
	 */
	public AnalyticsSpanBuilder addChildSpan(final AnalyticsSpan childSpan) {
		Assertion.check().isNotNull(childSpan, "the child span is required ");
		//---
		childSpans.add(childSpan);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsSpan build() {
		final Instant end = myEnd != null ? myEnd : Instant.now();
		return new AnalyticsSpan(
				myCategory,
				myName,
				start,
				end,
				measures,
				tags,
				childSpans);
	}
}
