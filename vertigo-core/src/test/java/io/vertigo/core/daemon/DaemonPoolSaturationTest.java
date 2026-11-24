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
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public final class DaemonPoolSaturationTest extends AbstractTestCaseJU5 {

	@Inject
	private AnalyticsManager analyticsManager;
	@Inject
	private DaemonManager daemonManager;
	@Inject
	private FakePoolSaturationComponent fakePoolSaturationComponent;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withDaemonMinDurationInSeconds(1)
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(FakePoolSaturationComponent.class)
						.build())
				.build();
	}

	@Override
	protected void doTearDown() {
		fakePoolSaturationComponent.releaseLatch().countDown();
	}

	@Test
	public void testSaturatedPoolIsYellowAndRecovers() throws InterruptedException {
		Thread.sleep(6500); // both threads blocked from t+1s : the victim waits for a free thread, limit min(max(4 x 1s, 1s), 7200s) = 4s
		// -----
		final HealthCheck saturated = findHealthCheck("poolUtilization");
		Assertions.assertEquals(HealthStatus.YELLOW, saturated.healthMeasure().status());
		Assertions.assertTrue(saturated.healthMeasure().message().contains(FakePoolSaturationComponent.VICTIM_NAME));
		// -----
		final long victimCountBeforeRelease = findVictimStat().getCount();
		fakePoolSaturationComponent.releaseLatch().countDown();
		awaitVictimExecutionAfterRelease(victimCountBeforeRelease);
		final HealthCheck recovered = findHealthCheck("poolUtilization");
		Assertions.assertEquals(HealthStatus.GREEN, recovered.healthMeasure().status(), recovered.healthMeasure().message());
	}

	private void awaitVictimExecutionAfterRelease(final long victimCountBeforeRelease) throws InterruptedException {
		final long deadline = System.currentTimeMillis() + 10_000;
		while (System.currentTimeMillis() < deadline) {
			// the victim may have run once before the saturation : wait for an execution after the release
			if (findVictimStat().getCount() > victimCountBeforeRelease) {
				return;
			}
			Thread.sleep(100);
		}
		Assertions.fail("the victim daemon never ran after the pool was released");
	}

	private DaemonStat findVictimStat() {
		return daemonManager.getStats().stream()
				.filter(stat -> FakePoolSaturationComponent.VICTIM_NAME.equals(stat.getDaemonName()))
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
