package io.vertigo.database.timeseries;

import java.time.Instant;
import java.util.Map;

import io.vertigo.lang.Assertion;

/**
 * Measure
 *
 */
public final class Measure {

	/**
	 * Create a new Point Build build to create a new Point in a fluent manner.
	 *
	 * @param measurement
	 *            the name of the measurement.
	 * @return the Builder to be able to add further Builder calls.
	 */

	public static MeasureBuilder builder(final String measurement) {
		return new MeasureBuilder(measurement);
	}

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

	public Map<String, Object> getFields() {
		return fields;
	}

	public Instant getInstant() {
		return instant;
	}

	public String getMeasurement() {
		return measurement;
	}

	public Map<String, String> getTags() {
		return tags;
	}

}
