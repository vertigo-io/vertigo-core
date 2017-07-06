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
import io.vertigo.commons.health.data.RedisChecker;
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
						.withHealthManager()
						.withRedisConnector(redisHost, redisPort, redisDatabase, Optional.empty())
						.build())
				.addModule(ModuleConfig.builder("checkers")
						.addComponent(RedisChecker.class)
						.addComponent(FailedComponentChecker.class)
						.addComponent(SuccessComponentChecker.class)
						.build())
				.build();
	}

	@Test
	void testRedisChecker() {
		final List<HealthControlPoint> redisControlPoints = findControlPointsByName("redisChecker.ping");
		//---
		Assert.assertEquals(1, redisControlPoints.size());
		Assert.assertEquals(HealthStatus.GREEN, redisControlPoints.get(0).getStatus());

	}

	@Test
	void testFailComponent() {
		final List<HealthControlPoint> failedControlPoints = findControlPointsByName(FailedComponentChecker.class.getSimpleName());
		//---
		Assert.assertEquals(1, failedControlPoints.size());
		Assert.assertEquals(HealthStatus.RED, failedControlPoints.get(0).getStatus());
		Assert.assertTrue(failedControlPoints.get(0).getCause() instanceof VSystemException);
	}

	@Test
	void testSuccessComponent() {
		final List<HealthControlPoint> successControlPoints = findControlPointsByName(SuccessComponentChecker.class.getSimpleName());
		//---
		Assert.assertEquals(1, successControlPoints.size());
		Assert.assertEquals(HealthStatus.GREEN, successControlPoints.get(0).getStatus());
	}

	@Test
	void testAggregate() {
		final List<HealthControlPoint> successControlPoints = findControlPointsByName(SuccessComponentChecker.class.getSimpleName());
		final List<HealthControlPoint> failedControlPoints = findControlPointsByName(FailedComponentChecker.class.getSimpleName());
		//---
		Assert.assertEquals(HealthStatus.GREEN, healthManager.aggregate(successControlPoints));
		Assert.assertEquals(HealthStatus.RED, healthManager.aggregate(failedControlPoints));

	}

	private List<HealthControlPoint> findControlPointsByName(final String name) {
		Assertion.checkArgNotEmpty(name);
		//---
		return healthManager.getControlPoints()
				.stream()
				.filter(controlPoint -> name.equals(controlPoint.getName()))
				.collect(Collectors.toList());
	}

}
