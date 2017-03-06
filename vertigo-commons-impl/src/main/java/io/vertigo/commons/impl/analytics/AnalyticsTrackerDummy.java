package io.vertigo.commons.impl.analytics;

import io.vertigo.commons.analytics.AnalyticsTracker;

/**
 * Dummy implementation of a tracker.
 * Used when collect is disabled.
 * @author mlaroche
 *
 */
final class AnalyticsTrackerDummy implements AnalyticsTracker {

	protected static final AnalyticsTracker DUMMY_TRACKER = new AnalyticsTrackerDummy();

	private AnalyticsTrackerDummy() {
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker incMeasure(final String measureType, final double value) {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker setMeasure(final String measureType, final double value) {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AnalyticsTracker addMetaData(final String metaDataName, final String value) {
		return this;
	}

}
