package io.vertigo.commons.health;

import java.util.function.Supplier;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.lang.Assertion;

/**
 * This definition defines a control point to check health of the app.
 * A control point is  :
 * 			- a definition name
 * 			- a control point name
 * 			- a checker
 * 			- a way to test that provides a HealthMeasure
 * @author mlaroche, pchretien
 */
@DefinitionPrefix("HCHK")
public final class HealthCheckDefinition implements Definition {

	private final String definitionName;
	private final String controlPointName;
	private final String checker;
	private final Supplier<HealthMeasure> checkMethod;

	/**
	 * Constructor
	 * @param definitionName the definitionName of the definition(must be unique)
	 * @param healthCheckName the name of the control point(must be unique)
	 * @param checker the name of the component that is responsible of retrieving the measure
	 * @param checkMethod the supplier of the healthMeasure
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
		controlPointName = healthCheckName;
		this.checker = checker;
		this.checkMethod = checkMethod;
	}

	/**
	 * Return the name of the checker
	 * @return the checker
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
	 * @return the control point name;
	 */
	public String getHealthCheckName() {
		return controlPointName;
	}

	@Override
	public String getName() {
		return definitionName;
	}

}
