package io.vertigo.database.timeseries;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import io.vertigo.lang.Assertion;

/**
 * Measure
 *
 */
public class Measure {
	private final String measurement;
	private final Instant instant;
	private final Map<String, Object> fields;
	private final Map<String, String> tags;

	Measure(
			final String measurement,
			final Instant instant,
			final Map<String, Object> fields,
			final Map<String, String> tags) {
		Assertion.checkArgNotEmpty(measurement);
		Assertion.checkNotNull(instant);
		Assertion.checkState(fields.size() > 0, "At least one field is required on a measure");
		//---
		this.measurement = measurement;
		this.instant = instant;
		this.fields = fields;
		this.tags = tags;
	}

	/**
	 * Create a new Point Build build to create a new Point in a fluent manner.
	 *
	 * @param measurement
	 *            the name of the measurement.
	 * @return the Builder to be able to add further Builder calls.
	 */

	public static Builder builder(final String measurement) {
		return new Builder(measurement);
	}

	public String getMeasurement() {
		return measurement;
	}

	public Instant getInstant() {
		return instant;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	/**
	 * Builder for a new Point.
	 *
	 * @author stefan.majer [at] gmail.com
	 *
	 */
	public static final class Builder {
		private final String measurement;
		private final Map<String, String> tags = new TreeMap<>();
		private Instant instant;
		private final Map<String, Object> fields = new TreeMap<>();

		/**
		 * @param measurement
		 */
		Builder(final String measurement) {
			this.measurement = measurement;
		}

		/**
		 * Add a tag to this point.
		 *
		 * @param tagName
		 *            the tag name
		 * @param value
		 *            the tag value
		 * @return the Builder instance.
		 */
		public Builder tag(final String tagName, final String value) {
			Assertion.checkArgNotEmpty(tagName);
			Assertion.checkNotNull(value);
			//---
			tags.put(tagName, value);
			return this;
		}

		public Builder addField(final String field, final boolean value) {
			Assertion.checkArgNotEmpty(field);
			Assertion.checkNotNull(value);
			//---
			fields.put(field, value);
			return this;
		}

		public Builder addField(final String field, final long value) {
			Assertion.checkArgNotEmpty(field);
			Assertion.checkNotNull(value);
			//---
			fields.put(field, value);
			return this;
		}

		public Builder addField(final String field, final double value) {
			Assertion.checkArgNotEmpty(field);
			Assertion.checkNotNull(value);
			//---
			fields.put(field, value);
			return this;
		}

		public Builder addField(final String field, final String value) {
			Assertion.checkArgNotEmpty(field);
			Assertion.checkNotNull(value);
			//---
			fields.put(field, value);
			return this;
		}

		/**
		 * Add a time to this point.
		 *
		 * @param timeToSet the time for this point
		 * @param precisionToSet the TimeUnit
		 * @return the Builder instance.
		 */
		public Builder time(final Instant measureInstant) {
			Assertion.checkNotNull(measureInstant);
			//---
			instant = measureInstant;
			return this;
		}

		/**
		 * Create a new Point.
		 *
		 * @return the newly created Point.
		 */
		public Measure build() {
			return new Measure(measurement, instant, fields, tags);
		}
	}

}
