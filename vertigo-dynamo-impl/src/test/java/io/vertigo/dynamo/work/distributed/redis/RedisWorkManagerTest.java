package io.vertigo.dynamo.work.distributed.redis;

import io.vertigo.dynamo.plugins.work.redis.RedisUtil;
import io.vertigo.dynamo.work.AbstractWorkManagerTest;
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

	//
	//	@Test
	//	public void ping() {
	//		final JedisPool jedisPool = RedisUtil.createJedisPool("localhost", 6379);
	//		final PrintStream out = System.out;
	//		//		for (int t = 0; t < 1; t++) {
	//		//			out.println("new thread ");
	//		//			Thread thread = new Thread(new Runnable() {
	//		//				public void run() {
	//		for (int i = 0; i < 300; i++) {
	//			try (final Jedis jedis = jedisPool.getResource()) {
	//				//if (i == 0 || i == 100) {
	//				out.println(" thread : " + Thread.currentThread().getId());
	//				//	}
	//				jedis.ping();
	//			}
	//		}
	//		//				}
	//		//			});
	//		//
	//		//			thread.start();
	//		//		}
	//		//		Thread.sleep(5000);
	//	}

	private static void reset(final JedisPool jedisPool) {
		final Jedis jedis = jedisPool.getResource();
		try {
			jedis.flushAll();
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
}
