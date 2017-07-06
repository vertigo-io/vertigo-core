/**
 *
 */
package io.vertigo.commons.health;

import java.util.List;

import io.vertigo.core.component.Component;

/**
 * This component checks the health of the current application.
 *
 * @author jmforhan
 */
public interface HealthManager extends Component {
	/**
	 * @return the list of health checks
	 */
	List<HealthCheck> getHealthChecks();

	/**
	 * Generates an aggregated status from a list of health checks.
	 *
	 * @param healthChecks the list of halth checks.
	 * @return the global health status
	 */
	HealthStatus aggregate(List<HealthCheck> healthChecks);
}
