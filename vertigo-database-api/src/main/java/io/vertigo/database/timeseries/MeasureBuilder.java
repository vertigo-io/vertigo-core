package io.vertigo.database.timeseries;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * Builder for a new Point.
 *
 * @author stefan.majer [at] gmail.com
 *
 */
public final class MeasureBuilder implements Builder<Measure> {
	private final String measurement;
	private final Map<String, String> tags = new TreeMap<>();
	private Instant instant;
	private final Map<String, Object> fields = new TreeMap<>();

	/**
	 * @param measurement
	 */
	MeasureBuilder(final String measurement) {
		Assertion.checkArgNotEmpty(measurement);
		//---
		this.measurement = measurement;
	}

	public MeasureBuilder addField(final String field, final boolean value) {
		Assertion.checkArgNotEmpty(field);
		Assertion.checkNotNull(value);
		//---
		fields.put(field, value);
		return this;
	}

	public MeasureBuilder addField(final String field, final double value) {
		Assertion.checkArgNotEmpty(field);
		Assertion.checkNotNull(value);
		//---
		fields.put(field, value);
		return this;
	}

	public MeasureBuilder addField(final String field, final long value) {
		Assertion.checkArgNotEmpty(field);
		Assertion.checkNotNull(value);
		//---
		fields.put(field, value);
		return this;
	}

	public MeasureBuilder addField(final String field, final String value) {
		Assertion.checkArgNotEmpty(field);
		Assertion.checkNotNull(value);
		//---
		fields.put(field, value);
		return this;
	}

	/**
	 * Create a new Point.
	 *
	 * @return the newly created Point.
	 */
	@Override
	public Measure build() {
		return new Measure(measurement, instant, fields, tags);
	}

	/**
	 * Add a tag to this point.
	 *
	 * @param tagName the tag name
	 * @param value the tag value
	 * @return the Builder instance.
	 */
	public MeasureBuilder tag(final String tagName, final String value) {
		Assertion.checkArgNotEmpty(tagName);
		Assertion.checkNotNull(value);
		//---
		tags.put(tagName, value);
		return this;
	}

	/**
	 * Add a time to this point.
	 *
	 * @param timeToSet the time for this point
	 * @param precisionToSet the TimeUnit
	 * @return the Builder instance.
	 */
	public MeasureBuilder time(final Instant measureInstant) {
		Assertion.checkNotNull(measureInstant);
		//---
		instant = measureInstant;
		return this;
	}
}
