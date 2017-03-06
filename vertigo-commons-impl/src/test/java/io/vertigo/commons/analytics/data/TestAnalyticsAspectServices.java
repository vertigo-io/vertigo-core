package io.vertigo.commons.analytics.data;

import javax.inject.Inject;

import io.vertigo.commons.analytics.Analytics;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;

public class TestAnalyticsAspectServices implements Component {

	@Inject
	private AnalyticsManager analyticsManager;

	@Analytics(processType = "test", category = "add")
	public int add(final int a, final int b) {

		return a + b;
	}

	@Analytics(processType = "test", category = "checkPositive")
	public void checkPositive(final int a) {
		Assertion.checkState(a >= 0, "The number must be positive");
	}

	@Analytics(processType = "test", category = "setMeasure")
	public void setMeasure() {
		analyticsManager.getCurrentTracker()
				.ifPresent(tracker -> tracker.setMeasure("price", 100));
	}

	@Analytics(processType = "test", category = "setMeasure")
	public void setAndIncMeasure() {
		analyticsManager.getCurrentTracker()
				.ifPresent(tracker -> tracker.setMeasure("price", 100));
		analyticsManager.getCurrentTracker()
				.ifPresent(tracker -> tracker.incMeasure("price", 10));
		analyticsManager.getCurrentTracker()
				.ifPresent(tracker -> tracker.incMeasure("price", 10));
	}

	@Analytics(processType = "test", category = "incMeasure")
	public void incMeasure() {
		analyticsManager.getCurrentTracker()
				.ifPresent(tracker -> tracker.incMeasure("price", 10));
	}

}
