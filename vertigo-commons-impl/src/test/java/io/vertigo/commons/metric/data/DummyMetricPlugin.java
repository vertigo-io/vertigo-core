package io.vertigo.commons.metric.data;

import java.util.Collections;
import java.util.List;

import io.vertigo.commons.metric.Metric;
import io.vertigo.commons.metric.MetricPlugin;

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
