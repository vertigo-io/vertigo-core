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
	 * @return the list of control points
	 */
	List<HealthControlPoint> getControlPoints();

	/**
	 * Generates an aggregated status from a list of control points.
	 *
	 * @param controlPoints the list of control points.
	 * @return the global health status
	 */
	HealthStatus aggregate(List<HealthControlPoint> controlPoints);
}
