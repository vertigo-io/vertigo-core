/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.analytics.metric.data;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.metric.MetricBuilder;
import io.vertigo.core.analytics.metric.Metrics;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Component;

/**
 * Composant to provide Metrics about system
 *
 * @author mlaroche
 */
public final class SystemMetricsProvider implements Component {

	private static final String SYSTEM_TOPIC = "system";

	/**
	 * Constructor.
	 */
	@Inject
	public SystemMetricsProvider() {
		//
	}

	@Metrics
	public List<Metric> getSystemMetrics() {
		final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		final Runtime runtime = Runtime.getRuntime();
		//---
		final Metric memoryUsed = buildMetric("memoryUsed", () -> Long.valueOf(runtime.totalMemory() / 1024 / 1024).doubleValue());
		final Metric memoryUsedPercent = buildMetric("memoryUsedPercent", () -> Long.valueOf(runtime.totalMemory()).doubleValue() / runtime.maxMemory() * 100);
		final Metric cpuUsage = buildMetric("cpuUsage", () -> operatingSystemMXBean.getSystemLoadAverage());
		//---
		return Arrays.asList(memoryUsed, memoryUsedPercent, cpuUsage);

	}

	private static Metric buildMetric(final String name, final Supplier<Double> supplier) {
		Assertion.check().isNotBlank(name);
		//---
		final MetricBuilder metricBuilder = Metric.builder()
				.withName(name)
				.withFeature(SYSTEM_TOPIC);
		try {
			metricBuilder
					.withValue(supplier.get())
					.withSuccess();
		} catch (final Exception e) {
			metricBuilder.withError();
		}
		return metricBuilder.build();
	}

}
