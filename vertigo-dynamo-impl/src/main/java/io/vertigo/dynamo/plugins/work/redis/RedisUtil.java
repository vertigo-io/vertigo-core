/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.kernel.lang.Option;

import java.io.Serializable;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
  * @author pchretien
 * $Id: RedisUtil.java,v 1.7 2014/06/26 12:30:08 npiedeloup Exp $
 */
public final class RedisUtil {
	private static final int timeout = 2000;
	private static final CodecManager codecManager = new CodecManagerImpl();

	public static JedisPool createJedisPool(final String redisHost, final int port, final Option<String> password) {
		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		//jedisPoolConfig.setMaxActive(10);
		final JedisPool jedisPool;
		if (password.isDefined()) {
			jedisPool = new JedisPool(jedisPoolConfig, redisHost, port, timeout, password.get());
		} else {
			jedisPool = new JedisPool(jedisPoolConfig, redisHost, port, timeout);
		}

		//test
		try (Jedis jedis = jedisPool.getResource()) {
			//final String ping = jedis.ping();
			jedis.ping();
			//System.out.println(" ping=" + ping);
		}
		return jedisPool;
	}

	static String encode(final Object toEncode) {
		return codecManager.getBase64Codec().encode(codecManager.getSerializationCodec().encode((Serializable) toEncode));
	}

	static Object decode(final String encoded) {
		return codecManager.getSerializationCodec().decode(codecManager.getBase64Codec().decode(encoded));
	}
}
