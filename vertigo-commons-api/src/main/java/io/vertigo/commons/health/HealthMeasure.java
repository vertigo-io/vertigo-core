/**
 *
 */
package io.vertigo.commons.health;

import io.vertigo.lang.Assertion;

/**
 *  Measure.
 *  example :
 * Ping to a Database produces a specific Measure
 *
 * @author jmforhan
 */
public final class HealthMeasure {
	private final HealthStatus status;
	private final String message; //may be null
	private final Exception cause; //may be null

	/**
	 * Creates the builder.
	 * @return the builder
	 */
	public static HealthMeasureBuilder builder() {
		return new HealthMeasureBuilder();
	}

	/**
	 * Constructor.
	 *
	 * @param status the status returned by the test
	 * @param message a message that explains the status (should be required whenever the status is not GREEN)
	 * @param cause the exception throwed during status checking (Should be empty with a GREEN status)
	 */
	HealthMeasure(
			final HealthStatus status,
			final String message,
			final Exception cause) {
		Assertion.checkNotNull(status);
		//-----
		this.status = status;
		/*
		 * message and cause can ne null or non null.
		 */
		this.message = message;
		this.cause = cause;
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
