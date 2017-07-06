package io.vertigo.commons.health.data;

import javax.inject.Inject;

import io.vertigo.commons.health.HealthChecked;
import io.vertigo.commons.health.HealthMeasure;
import io.vertigo.commons.health.HealthMeasureBuilder;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.core.component.Component;
import io.vertigo.lang.Assertion;
import redis.clients.jedis.Jedis;

public class RedisHealthChecker implements Component {

	private final RedisConnector redisConnector;

	@Inject
	public RedisHealthChecker(final RedisConnector redisConnector) {
		Assertion.checkNotNull(redisConnector);
		//---
		this.redisConnector = redisConnector;
	}

	@HealthChecked(name = "redisHealthChecker.ping")
	public HealthMeasure checkRedisPing() {
		final HealthMeasureBuilder healthControlPointBuilder = HealthMeasure.builder();
		try (Jedis jedis = redisConnector.getResource()) {
			jedis.ping();
			healthControlPointBuilder.withGreenStatus();
		} catch (final Exception e) {
			healthControlPointBuilder.withRedStatus(e.getMessage(), e);
		}
		return healthControlPointBuilder.build();

	}

}
