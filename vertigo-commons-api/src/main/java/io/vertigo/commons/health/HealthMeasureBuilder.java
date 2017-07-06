package io.vertigo.commons.health;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This builder must be used to build a control point.
 * @author pchretien
 *
 */
public final class HealthMeasureBuilder implements Builder<HealthMeasure> {
	private HealthStatus myStatus;
	private String myMessage; //may be null
	private Exception myCause; //may be null

	HealthMeasureBuilder() {
		//package : use the builder instead
		super();
	}

	/**
	 * @return this builder
	 */
	public HealthMeasureBuilder withGreenStatus() {
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
	public HealthMeasureBuilder withYellowStatus(final String message, final Exception cause) {
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
	public HealthMeasureBuilder withRedStatus(final String message, final Exception cause) {
		Assertion.checkState(myStatus == null, "status already set");
		//---
		myStatus = HealthStatus.RED;
		myMessage = message;
		myCause = cause;
		return this;
	}

	@Override
	public HealthMeasure build() {
		return new HealthMeasure(myStatus, myMessage, myCause);
	}
}
