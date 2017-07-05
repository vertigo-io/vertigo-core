/**
 *
 */
package io.vertigo.commons.health;

import java.util.List;

/**
 * This interface must be used on each component to check its health.
 *
 * @author jmforhan
 */
public interface HealthComponentStatusSupplier {

	/**
	 * @return the list of control points
	 */
	List<HealthControlPoint> getControlPoints();
}
