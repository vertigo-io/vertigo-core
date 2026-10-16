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

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.health.HealthStatus;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public final class DaemonRunningNotStarvedTest extends AbstractTestCaseJU5 {

	@Inject
	private AnalyticsManager analyticsManager;
	@Inject
	private DaemonManager daemonManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(FakeSlowComponent.class)
						.build())
				.build();
	}

	@Test
	public void runningDaemonIsNotOverdue() throws InterruptedException {
		Thread.sleep(2500); // first exec starts at t+1s and runs for 3s: it is running at t+2.5s

		final DaemonStat daemonStat = daemonManager.getStats().stream()
				.filter(stat -> FakeSlowComponent.SLOW_DAEMON_NAME.equals(stat.getDaemonName()))
				.findFirst()
				.orElseThrow();
		Assertions.assertEquals(DaemonStat.Status.running, daemonStat.getStatus());

		// a running daemon owns its thread: it is not overdue, even though its run already exceeds its 1s period
		final DaemonPoolStat poolStat = daemonManager.getPoolStat();
		Assertions.assertEquals(0, poolStat.overdueTasks());
		Assertions.assertEquals(0L, poolStat.maxOverdueMillis());

		// and it is not dead/stuck: no RED
		final HealthCheck daemonsAlive = findHealthCheck("daemonsAlive");
		Assertions.assertEquals(HealthStatus.GREEN, daemonsAlive.healthMeasure().status());
	}

	private HealthCheck findHealthCheck(final String name) {
		return analyticsManager.getHealthChecks()
				.stream()
				.filter(healthCheck -> "daemons".equals(healthCheck.feature()) && name.equals(healthCheck.name()))
				.findFirst()
				.orElseThrow();
	}

}
