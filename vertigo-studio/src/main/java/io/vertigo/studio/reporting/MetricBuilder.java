package io.vertigo.studio.reporting;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.studio.reporting.Metric.Status;

/**
 * Builder de metric.
 *
 * @author pchretien
 */
public final class MetricBuilder implements Builder<Metric> {
	private Status myStatus = Status.Executed; //default not required
	private String myTitle; //require
	private String myUnit = ""; //default not required
	private Object myValue = null; //default not required
	private String myValueInformation = null; //default not required

	@Override
	public Metric build() {
		return new Metric(myStatus, myTitle, myUnit, myValue, myValueInformation);
	}

	/**
	 * Status de la métrique.
	 */
	public MetricBuilder withStatus(final Status status) {
		Assertion.checkNotNull(status);
		//---------------------------------------------------------------------
		myStatus = status;
		return this;
	}

	/**
	 * Titre de la métrique. (notNull)
	 */
	public MetricBuilder withTitle(final String title) {
		Assertion.checkArgNotEmpty(title);
		//---------------------------------------------------------------------
		myTitle = title;
		return this;
	}

	/**
	 * Unité de la métrique. (notNull)
	 */
	public MetricBuilder withUnit(final String unit) {
		Assertion.checkArgNotEmpty(unit);
		//---------------------------------------------------------------------
		myUnit = unit;
		return this;
	}

	/**
	 * Valeur de la métrique. (Integer, Long, String, etc..)
	 */
	public MetricBuilder withValue(final Object value) {
		myValue = value;
		return this;
	}

	/**
	 * @return Complément d'information sur la valeur. (nullable)
	 */
	public MetricBuilder withValueInformation(final String valueInformation) {
		myValueInformation = valueInformation;
		return this;
	}
}
