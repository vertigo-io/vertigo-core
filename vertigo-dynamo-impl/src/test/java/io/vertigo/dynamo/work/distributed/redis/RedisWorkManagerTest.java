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
