/**
 *
 */
package io.vertigo.commons.health;

import io.vertigo.lang.Assertion;

/**
 * Control point.
 *  example :
 *  dataStorePlugin.ping : Ping to a Database produces a specific control point
 *  {plugin/component}.{test} :
 *
 * @author jmforhan
 */
public final class HealthControlPoint {
	private final String name;
	private final HealthStatus status;
	private final String message; //may be null
	private final Exception cause; //may be null

	/**
	 * Creates the builder.
	 * @param name the control point name
	 * @return the builder
	 */
	public static HealthControlPointBuilder of(final String name) {
		return new HealthControlPointBuilder(name);
	}

	/**
	 * Constructor.
	 *
	 * @param name the control point name
	 * @param status the status returned by the test
	 * @param message a message that explains the status (should be required whenever the status is not GREEN)
	 * @param cause the exception throwed during status checking (Should be empty with a GREEN status)
	 */
	HealthControlPoint(
			final String name,
			final HealthStatus status,
			final String message,
			final Exception cause) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(status);
		//-----
		this.name = name;
		this.status = status;
		/*
		 * message and cause can ne null or non null.
		 */
		this.message = message;
		this.cause = cause;
	}

	/**
	 * @return the control point name
	 */
	public String getComponentName() {
		return name;
	}

	/**
	 * @return the status
	 */
	public HealthStatus getStatus() {
		return status;
	}

	/**
	 * @return the message (may be null)
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the cause exception (may be null)
	 */
	public Exception getCause() {
		return cause;
	}
}
