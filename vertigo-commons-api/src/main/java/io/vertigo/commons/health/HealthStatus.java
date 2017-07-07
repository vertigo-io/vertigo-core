/**
 *
 */
package io.vertigo.commons.health;

/**
 * This class lists the health status.
 *
 * @author jmforhan
 */
public enum HealthStatus {
	/**
	 * green : the component is fully operational.
	 */
	GREEN,
	/**
	 * yellow : the component is partially operational.
	 */
	YELLOW,
	/**
	 * red : the component is not operational.
	 */
	RED
}
