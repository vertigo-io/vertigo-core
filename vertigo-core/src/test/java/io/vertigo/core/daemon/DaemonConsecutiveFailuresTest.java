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

public final class DaemonConsecutiveFailuresTest extends AbstractTestCaseJU5 {

	@Inject
	private AnalyticsManager analyticsManager;
	@Inject
	private DaemonManager daemonManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(FakeFailingComponent.class)
						.build())
				.build();
	}

	@Test
	public void consecutiveFailuresAreReportedWithoutRed() throws InterruptedException {
		Thread.sleep(3500); // 3 executions of the 1s-period failing daemon

		final DaemonStat failing = findDaemonStat(FakeFailingComponent.FAILING_DAEMON_NAME);
		Assertions.assertTrue(failing.getFailures() >= 3, "the failing daemon must have failed at least 3 times: " + failing);
		Assertions.assertTrue(failing.getConsecutiveFailures() >= 3, "the consecutive failures must be tracked: " + failing);
		//---
		// a daemon failing repeatedly (e.g. a dead external service) is a load/dependency problem, not a liveness
		// problem: it must be reported, never a RED (a restart cannot fix it)
		final HealthCheck lastExecs = findHealthCheck("lastExecs");
		Assertions.assertEquals(HealthStatus.YELLOW, lastExecs.healthMeasure().status());
		final HealthCheck daemonsAlive = findHealthCheck("daemonsAlive");
		Assertions.assertEquals(HealthStatus.GREEN, daemonsAlive.healthMeasure().status());
	}

	private DaemonStat findDaemonStat(final String daemonName) {
		return daemonManager.getStats().stream()
				.filter(stat -> daemonName.equals(stat.getDaemonName()))
				.findFirst()
				.orElseThrow();
	}

	private HealthCheck findHealthCheck(final String name) {
		return analyticsManager.getHealthChecks()
				.stream()
				.filter(healthCheck -> "daemons".equals(healthCheck.feature()) && name.equals(healthCheck.name()))
				.findFirst()
				.orElseThrow();
	}

}
