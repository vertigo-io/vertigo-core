/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.daemon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public final class DaemonMetricsTest extends AbstractTestCaseJU5 {

	@Inject
	private AnalyticsManager analyticsManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("myAspects")
						.addAspect(DaemonFakeAspect.class)
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(FakeComponent.class)
						.build())
				.build();
	}

	@Test
	public void testPoolMetricsByWindow() throws InterruptedException {
		analyticsManager.getMetrics(); // starts a new window
		Thread.sleep(3000); // first execution of the 2s-period daemon at t+2s
		// -----
		final List<Metric> metrics = analyticsManager.getMetrics();
		assertEquals(2.0, findDaemonMetric(metrics, "daemonPoolSize").orElseThrow().value());
		assertTrue(findDaemonMetric(metrics, "daemonExecutions").orElseThrow().value() >= 1.0);
		assertTrue(findDaemonMetric(metrics, "daemonAvgDuration").isPresent());
		// -----
		// the previous call consumed the window : the new one is empty
		final List<Metric> nextMetrics = analyticsManager.getMetrics();
		assertEquals(0.0, findDaemonMetric(nextMetrics, "daemonExecutions").orElseThrow().value());
		assertTrue(findDaemonMetric(nextMetrics, "daemonAvgDuration").isEmpty());
	}

	private static Optional<Metric> findDaemonMetric(final List<Metric> metrics, final String name) {
		return metrics.stream()
				.filter(metric -> "daemons".equals(metric.feature()) && name.equals(metric.name()))
				.findFirst();
	}

}
