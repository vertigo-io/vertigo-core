package io.vertigo.commons.health.data;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.commons.health.HealthComponentStatusSupplier;
import io.vertigo.commons.health.HealthControlPoint;
import io.vertigo.commons.health.HealthControlPointBuilder;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.core.component.Component;
import io.vertigo.lang.Assertion;
import redis.clients.jedis.Jedis;

public class RedisChecker implements Component, HealthComponentStatusSupplier {

	private final RedisConnector redisConnector;

	@Inject
	public RedisChecker(final RedisConnector redisConnector) {
		Assertion.checkNotNull(redisConnector);
		//---
		this.redisConnector = redisConnector;
	}

	@Override
	public List<HealthControlPoint> getControlPoints() {
		final HealthControlPointBuilder healthControlPointBuilder = HealthControlPoint.of("redisChecker.ping");
		try (Jedis jedis = redisConnector.getResource()) {
			jedis.ping();
			healthControlPointBuilder.withGreenStatus();
		} catch (final Exception e) {
			healthControlPointBuilder.withRedStatus(e.getMessage(), e);
		}
		return Collections.singletonList(healthControlPointBuilder.build());

	}

}
