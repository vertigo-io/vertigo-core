/**
 *
 */
package io.vertigo.commons.health;

import io.vertigo.lang.Assertion;

/**
 * Health check.
 *  example :
 *  dataStorePlugin.ping : Ping to a Database produces a specific control point with the actual measure of it
 *  {plugin/component}.{test} :
 *
 * @author mlaroche
 */
public final class HealthCheck {
	private final String name;
	private final String checker;
	private final HealthMeasure healthMeasure;

	/**
	 * Constructor.
	 *
	 * @param name the control point name
	 * @param checker who  created the measure
	 * @param healthMeasure the measure
	 */
	public HealthCheck(
			final String name,
			final String checker,
			final HealthMeasure healthMeasure) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(checker);
		Assertion.checkNotNull(healthMeasure);
		//-----
		this.name = name;
		this.checker = checker;
		this.healthMeasure = healthMeasure;
	}

	/**
	 * @return the control point name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the checker
	 */
	public String getChecker() {
		return checker;
	}

	/**
	 * @return the measure
	 */
	public HealthMeasure getMeasure() {
		return healthMeasure;
	}

}
