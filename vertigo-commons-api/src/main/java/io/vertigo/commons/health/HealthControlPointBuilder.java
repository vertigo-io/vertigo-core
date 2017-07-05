package io.vertigo.commons.health;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This builder must be used to build a control point.
 * @author pchretien
 *
 */
public final class HealthControlPointBuilder implements Builder<HealthControlPoint> {
	private final String name;
	private HealthStatus myStatus;
	private String myMessage; //may be null
	private Exception myCause; //may be null

	/**
	 * Constructor.
	 *
	 * @param name the control point name
	 */
	HealthControlPointBuilder(final String name) {
		Assertion.checkNotNull(name);
		//-----
		this.name = name;
	}

	/**
	 * @return this builder
	 */
	public HealthControlPointBuilder withGreenStatus() {
		Assertion.checkState(myStatus == null, "status already set");
		//---
		myStatus = HealthStatus.GREEN;
		return this;
	}

	/**
	 *
	 * @param message a message that explains the status
	 * @param cause the exception throwed during status test
	 * @return this builder
	 */
	public HealthControlPointBuilder withYellowStatus(final String message, final Exception cause) {
		Assertion.checkState(myStatus == null, "status already set");
		//---
		myStatus = HealthStatus.YELLOW;
		myMessage = message;
		myCause = cause;
		return this;
	}

	/**
	 * @param message a message that explains the status
	 * @param cause the exception throwed during status test
	 * @return this builder
	*/
	public HealthControlPointBuilder withRedStatus(final String message, final Exception cause) {
		Assertion.checkState(myStatus == null, "status already set");
		//---
		myStatus = HealthStatus.RED;
		myMessage = message;
		myCause = cause;
		return this;
	}

	@Override
	public HealthControlPoint build() {
		return new HealthControlPoint(name, myStatus, myMessage, myCause);
	}
}
