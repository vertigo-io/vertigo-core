package io.vertigo.dynamo.addons.connectors.redis;

import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.lang.Option;

import javax.inject.Inject;
import javax.inject.Named;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author pchretien
 */
public final class RedisConnector implements Component, Activeable {
	private static final int CONNECT_TIMEOUT = 2000;
	private final JedisPool jedisPool;

	@Inject
	public RedisConnector(
			final @Named("host") String redisHost,
			final @Named("port") int redisPort,
			final @Named("password") Option<String> passwordOption) {
		Assertion.checkArgNotEmpty(redisHost);
		Assertion.checkNotNull(passwordOption);
		//-----
		//this.readTimeout = timeoutSeconds;
		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		//jedisPoolConfig.setMaxActive(10);
		if (passwordOption.isDefined()) {
			jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, CONNECT_TIMEOUT, passwordOption.get());
		} else {
			jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, CONNECT_TIMEOUT);
		}
		//test
		try (Jedis jedis = jedisPool.getResource()) {
			jedis.ping();
			jedis.flushAll();
		}
	}

	public Jedis getResource() {
		return jedisPool.getResource();
	}

	@Override
	public void start() {
		//
	}

	@Override
	public void stop() {
		jedisPool.destroy();
	}

}
