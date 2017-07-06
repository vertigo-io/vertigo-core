package io.vertigo.commons.health;

import java.util.function.Supplier;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.lang.Assertion;

/**
 * This definition defines a health check
 * 			- a definition name
 * 			- a health check name
 * 			- a checker
 * 			- a way to test that provides a HealthMeasure
 * @author mlaroche, pchretien
 */
@DefinitionPrefix("HCHK")
public final class HealthCheckDefinition implements Definition {

	private final String definitionName;
	private final String healthCheckName;
	private final String checker;
	private final Supplier<HealthMeasure> checkMethod;

	/**
	 * Constructor
	 * @param definitionName the definition name (must be unique)
	 * @param healthCheckName the name of the health check (must be unique)
	 * @param checker the name of the component that is responsible of providing the measure
	 * @param checkMethod the check method that provides a health measure
	 */
	public HealthCheckDefinition(
			final String definitionName,
			final String healthCheckName,
			final String checker,
			final Supplier<HealthMeasure> checkMethod) {
		Assertion.checkArgNotEmpty(definitionName);
		Assertion.checkArgNotEmpty(healthCheckName);
		Assertion.checkArgNotEmpty(checker);
		Assertion.checkNotNull(checkMethod);
		//-----
		this.definitionName = definitionName;
		this.healthCheckName = healthCheckName;
		this.checker = checker;
		this.checkMethod = checkMethod;
	}

	/**
	 * @return the name of the health checker
	 */
	public String getChecker() {
		return checker;
	}

	/**
	 * @return the check method that provides a health measure
	 */
	public Supplier<HealthMeasure> getCheckMethod() {
		return checkMethod;
	}

	/**
	 * @return the health check name;
	 */
	public String getHealthCheckName() {
		return healthCheckName;
	}

	@Override
	public String getName() {
		return definitionName;
	}
}
