package io.vertigo.commons.analytics.data;

import javax.inject.Inject;

import io.vertigo.commons.analytics.Analytics;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;

public class TestAnalyticsAspectServices implements Component {

	@Inject
	private AnalyticsManager analyticsManager;

	@Analytics(category = "test", name = "add")
	public int add(final int a, final int b) {

		return a + b;
	}

	@Analytics(category = "test", name = "checkPositive")
	public void checkPositive(final int a) {
		Assertion.checkState(a >= 0, "The number must be positive");
	}

	@Analytics(category = "test", name = "setMeasure")
	public void setMeasure() {
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.setMeasure("price", 100));
	}

	@Analytics(category = "test", name = "setMeasure")
	public void setAndIncMeasure() {
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.setMeasure("price", 100));
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.incMeasure("price", 10));
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.incMeasure("price", 10));
	}

	@Analytics(category = "test", name = "incMeasure")
	public void incMeasure() {
		analyticsManager.getCurrentTracer()
				.ifPresent(tracer -> tracer.incMeasure("price", 10));
	}

}
