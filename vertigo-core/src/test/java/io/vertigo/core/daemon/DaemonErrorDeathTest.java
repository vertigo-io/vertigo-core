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

public final class DaemonErrorDeathTest extends AbstractTestCaseJU5 {

	@Inject
	private AnalyticsManager analyticsManager;
	@Inject
	private DaemonManager daemonManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withDaemonDeadAfterInSecond(2)
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(FakeErrorComponent.class)
						.build())
				.build();
	}

	@Test
	public void errorDeathIsDetectedByDaemonsAlive() throws InterruptedException {
		Thread.sleep(3000); // error at t+1s, dead detection (schedule done) is immediate

		// the Error escapes catch(Exception): WrappedException.wrap unwraps the InvocationTargetException
		// and rethrows the raw Error, so the periodic schedule is silently cancelled by the JDK :
		// counters stay frozen and the status is stuck at "running"
		final DaemonStat daemonStat = daemonManager.getStats().stream()
				.filter(stat -> FakeErrorComponent.ERROR_DAEMON_NAME.equals(stat.getDaemonName()))
				.findFirst()
				.orElseThrow();
		Assertions.assertEquals(0, daemonStat.getFailures());
		Assertions.assertEquals(DaemonStat.Status.running, daemonStat.getStatus());

		// the daemonsAlive check must detect the dead daemon immediately (schedule done),
		// not wait the stuck threshold with a misleading "stuck" message
		final HealthCheck daemonsAlive = findHealthCheck("daemonsAlive");
		Assertions.assertEquals(HealthStatus.RED, daemonsAlive.healthMeasure().status());
		Assertions.assertTrue(daemonsAlive.healthMeasure().message().contains(FakeErrorComponent.ERROR_DAEMON_NAME));
		Assertions.assertTrue(daemonsAlive.healthMeasure().message().contains("dead"));
	}

	private HealthCheck findHealthCheck(final String name) {
		return analyticsManager.getHealthChecks()
				.stream()
				.filter(healthCheck -> "daemons".equals(healthCheck.feature()) && name.equals(healthCheck.name()))
				.findFirst()
				.orElseThrow();
	}

}
