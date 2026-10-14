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
package io.vertigo.core.impl.daemon;

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.health.HealthStatus;
import io.vertigo.core.daemon.DaemonFakeAspect;
import io.vertigo.core.daemon.DaemonManager;
import io.vertigo.core.daemon.FakeComponent;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public final class DaemonShutdownHealthTest extends AbstractTestCaseJU5 {

	@Inject
	private AnalyticsManager analyticsManager;
	@Inject
	private DaemonManager daemonManager;

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
	public void shutdownPoolWhileActiveMakesHealthRed() throws Exception {
		// simulate an external shutdown of the pool, bypassing the manager's own stop
		final Field executorField = DaemonManagerImpl.class.getDeclaredField("daemonExecutor");
		executorField.setAccessible(true);
		final DaemonExecutor executor = (DaemonExecutor) executorField.get(daemonManager);
		executor.stop();
		//---
		final HealthCheck daemonsAlive = findHealthCheck("daemonsAlive");
		Assertions.assertEquals(HealthStatus.RED, daemonsAlive.healthMeasure().status());
		Assertions.assertTrue(daemonsAlive.healthMeasure().message().contains("shut down"));
	}

	@Test
	public void gracefulStopDoesNotMakeHealthRed() {
		((Activeable) daemonManager).stop();
		//---
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
