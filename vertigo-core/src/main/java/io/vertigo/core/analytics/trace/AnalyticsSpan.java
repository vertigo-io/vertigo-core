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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.vertigo.core.lang.Assertion;

/**
 * A trace is composed of spans
 * A trace is trIggered by an app
 *
 *	A span contains
 *	 - when : start, end, duration
 *	 - what : name, category
 *	 - data : measures, tags
 *   - a list of child spans
 *
 * 	[when]
 * - start timestamp
 * - end   timestamp
 *
 *	[what]
 * - category (examples : sql, tasks)
 * - name (examples : /create/movies)
 *
 * 	[data]
 * - measures
 * - tags
 *
 *  [span hierarchy]
 * - list of child spans (0..*)
 *
 * @author pchretien, npiedeloup
 * @version $Id: KProcess.java,v 1.8 2012/10/16 17:18:26 pchretien Exp $
 */
public final class AnalyticsSpan {
	/**
	 * REGEX used to define rules on category, measures and tags.
	 */
	private static final Pattern CATEGORY_REGEX = Pattern.compile("[a-z]+");
	private static final Pattern MEASURE_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]+");
	private static final Pattern TAG_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]+");

	public static final String CATEGORY_SEPARATOR = "/";
	private final String category; //ex : sql, page....

	private final String name; //what ex : accounts/search

	private final long start; //when
	private final long end; //when

	private final Map<String, Double> measures;
	private final Map<String, String> tags;
	private final List<AnalyticsSpan> childSpans;

	/**
	 * Constructor.
	 * @param category the category
	 * @param name  the name
	 * @param start the start instant
	 * @param end the end instant
	 * @param measures the measures
	 * @param tags the tags
	 * @param childSpans the list of child spans (0..*)
	 */
	AnalyticsSpan(
			final String category,
			final String name,
			final Instant start,
			final Instant end,
			final Map<String, Double> measures,
			final Map<String, String> tags,
			final List<AnalyticsSpan> childSpans) {
		Assertion.check()
				.isNotNull(category, "the category is required")
				.isNotNull(name, "the name is required")
				.isNotNull(start, "the start is required")
				.isNotNull(end, "the end is required")
				.isNotNull(measures, "the measures are required")
				.isNotNull(tags, "the tags are required")
				.isNotNull(childSpans, "the child spans are required");
		//---
		checkRegex(category, CATEGORY_REGEX, "process type");
		measures.keySet()
				.forEach(measureName -> checkRegex(measureName, MEASURE_REGEX, "measure name"));
		tags.keySet()
				.forEach(tagName -> checkRegex(tagName, TAG_REGEX, "metadata name"));
		//---
		this.category = category;
		this.name = name;
		this.start = start.toEpochMilli();
		this.end = end.toEpochMilli();
		this.measures = Map.copyOf(measures);
		this.tags = Map.copyOf(tags);
		this.childSpans = List.copyOf(childSpans);
	}

	/**
	 * Static method factory for the span builder
	 *
	 * @param category the category
	 * @param name the name
	 * @return the span builder
	 */
	public static AnalyticsSpanBuilder builder(final String category, final String name) {
		return new AnalyticsSpanBuilder(category, name);
	}

	/**
	 * Static method factory for the span builder
	 *
	 * @param category the category
	 * @param name the name
	 * @param start the span start
	 * @param end the span end
	 * @return the span builder
	 */
	public static AnalyticsSpanBuilder builder(final String category, final String name, final Instant start, final Instant end) {
		return new AnalyticsSpanBuilder(category, name, start, end);
	}

	private static void checkRegex(final String s, final Pattern pattern, final String info) {
		if (!pattern.matcher(s).matches()) {
			throw new IllegalArgumentException(info + " " + s + " must match regex :" + pattern.pattern());
		}
	}

	/**
	 * [what]
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return the span duration (in milliseconds)
	 */
	public long getDurationMillis() {
		return end - start;
	}

	/**
	 * [when]
	 * @return the span start timestamp in Millis
	 */
	public long getStart() {
		return start;
	}

	/**
	 * [when]
	 * @return the span end timestamp in Millis
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * @return the span measures
	 */
	public Map<String, Double> getMeasures() {
		return measures;
	}

	/**
	 * @return the span tags
	 */
	public Map<String, String> getTags() {
		return tags;
	}

	/**
	 * @return the list of child spans
	 */
	public List<AnalyticsSpan> getChildSpans() {
		return childSpans;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "{category :" + category + ", name :" + name + "}";
	}
}
