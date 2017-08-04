/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.commons.health;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.health.data.FailedComponentChecker;
import io.vertigo.commons.health.data.RedisHealthChecker;
import io.vertigo.commons.health.data.SuccessComponentChecker;
import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

@RunWith(JUnitPlatform.class)
public class HealthManagerTest extends AbstractTestCaseJU4 {

	@Inject
	private HealthManager healthManager;

	@Override
	protected AppConfig buildAppConfig() {
		final String redisHost = "redis-pic.part.klee.lan.net";
		final int redisPort = 6379;
		final int redisDatabase = 15;

		return AppConfig.builder()
				.beginBoot()
				.endBoot()
				.addModule(new CommonsFeatures()
						.withRedisConnector(redisHost, redisPort, redisDatabase, Optional.empty())
						.build())
				.addModule(ModuleConfig.builder("checkers")
						.addComponent(RedisHealthChecker.class)
						.addComponent(FailedComponentChecker.class)
						.addComponent(SuccessComponentChecker.class)
						.build())
				.build();
	}

	@Test
	void testRedisChecker() {
		final List<HealthCheck> redisHealthChecks = findHealthChecksByName("ping");
		//---
		Assert.assertEquals(1, redisHealthChecks.size());
		Assert.assertEquals(HealthStatus.GREEN, redisHealthChecks.get(0).getMeasure().getStatus());

	}

	@Test
	void testFailComponent() {
		final List<HealthCheck> failedHealthChecks = findHealthChecksByName("failure");
		//---
		Assert.assertEquals(1, failedHealthChecks.size());
		Assert.assertEquals(HealthStatus.RED, failedHealthChecks.get(0).getMeasure().getStatus());
		Assert.assertTrue(failedHealthChecks.get(0).getMeasure().getCause() instanceof VSystemException);
	}

	@Test
	void testSuccessComponent() {
		final List<HealthCheck> successHealthChecks = findHealthChecksByName("success");
		//---
		Assert.assertEquals(1, successHealthChecks.size());
		Assert.assertEquals(HealthStatus.GREEN, successHealthChecks.get(0).getMeasure().getStatus());
	}

	@Test
	void testAggregate() {
		final List<HealthCheck> successHealthChecks = findHealthChecksByName("success");
		final List<HealthCheck> failedHealthChecks = findHealthChecksByName("failure");
		//---
		Assert.assertEquals(HealthStatus.GREEN, healthManager.aggregate(successHealthChecks));
		Assert.assertEquals(HealthStatus.RED, healthManager.aggregate(failedHealthChecks));

	}

	private List<HealthCheck> findHealthChecksByName(final String name) {
		Assertion.checkArgNotEmpty(name);
		//---
		return healthManager.getHealthChecks()
				.stream()
				.filter(healthCheck -> name.equals(healthCheck.getName()))
				.collect(Collectors.toList());
	}
}
