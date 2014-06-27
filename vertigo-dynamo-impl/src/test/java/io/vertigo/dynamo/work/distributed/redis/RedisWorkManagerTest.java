package io.vertigo.dynamo.work.distributed.redis;

import io.vertigo.dynamo.work.AbstractWorkManagerTest;
import io.vertigo.dynamox.work.plugins.redis.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author pchretien
 * $Id: RedisWorkManagerTest.java,v 1.4 2014/01/20 18:57:06 pchretien Exp $
 */
public class RedisWorkManagerTest extends AbstractWorkManagerTest {
	@Override
	protected void doSetUp() throws Exception {
		reset(RedisUtil.createJedisPool("localhost", 6379));
		//reset(RedisUtil.createJedisPool("kasper-redis", 6379));
	}

	private static void reset(final JedisPool jedisPool) {
		final Jedis jedis = jedisPool.getResource();
		try {
			jedis.flushAll();
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
}
