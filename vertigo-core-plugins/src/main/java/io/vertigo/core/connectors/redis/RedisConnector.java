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
package io.vertigo.core.connectors.redis;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author pchretien
 */
public final class RedisConnector implements Component, Activeable {
	private static final int CONNECT_TIMEOUT = 2000;
	private final JedisPool jedisPool;

	/**
	 * Constructor.
	 * @param redisHost REDIS server host name
	 * @param redisPort REDIS server port
	 * @param redisDatabase REDIS database index
	 * @param passwordOption password (optional)
	 */
	@Inject
	public RedisConnector(
			@Named("host") final String redisHost,
			@Named("port") final int redisPort,
			@Named("database") final int redisDatabase,
			@Named("password") final Optional<String> passwordOption) {
		Assertion.checkArgNotEmpty(redisHost);
		Assertion.checkNotNull(passwordOption);
		Assertion.checkArgument(redisDatabase >= 0 && redisDatabase < 16, "there 16 DBs(0 - 15); your index database '{0}' is not inside this range", redisDatabase);
		//-----
		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, CONNECT_TIMEOUT, passwordOption.orElse(null), redisDatabase);
		//test
		try (Jedis jedis = jedisPool.getResource()) {
			jedis.ping();
		}
	}

	/**
	 * @return Redis resource
	 */
	public Jedis getResource() {
		return jedisPool.getResource();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		jedisPool.destroy();
	}

}
