package io.vertigo.commons.metric.data;

import java.util.Collections;
import java.util.List;

import io.vertigo.commons.impl.metric.MetricPlugin;
import io.vertigo.commons.metric.Metric;

public class DummyMetricPlugin implements MetricPlugin {

	@Override
	public List<Metric> analyze() {
		return Collections.singletonList(
				Metric
						.builder()
						.withType("type")
						.withSubject("subject")
						.withValue(0.00)
						.build());
	}

}
