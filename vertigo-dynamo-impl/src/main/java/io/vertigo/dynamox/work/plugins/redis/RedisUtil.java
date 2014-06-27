package io.vertigo.dynamox.work.plugins.redis;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.codec.CodecManagerImpl;

import java.io.Serializable;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
  * @author pchretien
 * $Id: RedisUtil.java,v 1.7 2014/06/26 12:30:08 npiedeloup Exp $
 */
public final class RedisUtil {
	private static final CodecManager codecManager = new CodecManagerImpl();

	public static JedisPool createJedisPool(final String redisHost, final int port) {
		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		//jedisPoolConfig.setMaxActive(10);
		final JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisHost, port);

		//test
		final Jedis jedis = jedisPool.getResource();
		try {
			//final String ping = jedis.ping();
			jedis.ping();
			//System.out.println(" ping=" + ping);
		} finally {
			jedisPool.returnResource(jedis);
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
