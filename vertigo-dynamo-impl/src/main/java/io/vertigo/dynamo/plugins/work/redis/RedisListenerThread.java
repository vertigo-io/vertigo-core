package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author pchretien
 * $Id: RedisListenerThread.java,v 1.6 2014/01/20 18:56:18 pchretien Exp $
 */
final class RedisListenerThread extends Thread {
	private final JedisPool jedisPool;
	private final Map<String, WorkResultHandler> workResultHandlers;

	RedisListenerThread(final JedisPool jedisPool, final Map<String, WorkResultHandler> workResultHandlers) {
		Assertion.checkNotNull(jedisPool);
		Assertion.checkNotNull(workResultHandlers);
		//-----------------------------------------------------------------
		this.jedisPool = jedisPool;
		this.workResultHandlers = workResultHandlers;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		while (!isInterrupted()) {
			//				int retry = 0;
			//				while (retry < 3) {
			Jedis jedis = jedisPool.getResource();
			try {
				//On attend le r�sultat (par tranches de 1s)
				final int waitTimeSeconds = 1;

				final String workId = jedis.brpoplpush("works:done", "works:completed", waitTimeSeconds);
				if (workId != null) {
					final WorkResultHandler workResultHandler = workResultHandlers.get(workId);
					if (workResultHandler != null) {
						//Que faire sinon 
						if ("ok".equals(jedis.hget("work:" + workId, "status"))) {
							workResultHandler.onSuccess(RedisUtil.decode(jedis.hget("work:" + workId, "result")));
						} else {
							final Throwable t = (Throwable) RedisUtil.decode(jedis.hget("work:" + workId, "error"));
							workResultHandler.onFailure(t);
						}
						//ZZZZZZZZZZZZZZZZZZZZ
						//ZZZZZZZZZZZZZZZZZZZZ
						//ZZZZZZZZZZZZZZZZZZZZ
						jedis.del("work:" + workId);
					}
				}

			} catch (final JedisException e) {
				jedisPool.returnBrokenResource(jedis);
				jedis = null;
			} finally {
				jedisPool.returnResource(jedis);
			}
			//throw new RuntimeException("redisListener");

			//					System.out.println("retry");
			//					retry++;
			//				}
			//				throw new RuntimeException("3 essais ");
		}
	}
}
