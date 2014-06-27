package io.vertigo.dynamox.work.plugins.redis;

import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author pchretien
 * $Id: RedisWorkResultHandler.java,v 1.7 2014/02/27 10:31:38 pchretien Exp $
 */
final class RedisWorkResultHandler<WR> implements WorkResultHandler<WR> {
	private final JedisPool jedisPool;
	private final String workId;
	private final boolean sync;

	RedisWorkResultHandler(final String workId, final JedisPool jedisPool, final boolean sync) {
		Assertion.checkNotNull(workId);
		Assertion.checkNotNull(jedisPool);
		//---------------------------------------------------------------------
		this.jedisPool = jedisPool;
		this.workId = workId;
		this.sync = sync;

	}

	/** {@inheritDoc} */
	public void onStart() {
		//			int retry = 0;
		//			while (retry < 3) {
		//				Jedis jedis = jedisPool.getResource();
		//				try {
		//					jedis.hset("work:" + workId, "status", "started");
		//					return;
		//				} catch (final JedisException e) {
		//					jedisPool.returnBrokenResource(jedis);
		//					jedis = null;
		//				} finally {
		//					if (jedis != null) {
		//						jedisPool.returnResource(jedis);
		//					}
		//				}
		//				System.out.println("retry");
		//				retry++;
		//			}
		//			throw new RuntimeException("3 essais ");
	}

	/** {@inheritDoc} */
	public void onSuccess(final WR result) {
		final Map<String, String> datas = new HashMap<>();
		datas.put("result", RedisUtil.encode(result));
		datas.put("status", "ok");
		exec(datas);
	}

	/** {@inheritDoc} */
	public void onFailure(final Throwable t) {
		final Map<String, String> datas = new HashMap<>();
		datas.put("error", RedisUtil.encode(t));
		datas.put("status", "ko");
		exec(datas);
	}

	private void exec(final Map<String, String> datas) {
		int retry = 0;
		while (retry < 3) {
			Jedis jedis = jedisPool.getResource();
			try {
				final Transaction tx = jedis.multi();
				tx.hmset("work:" + workId, datas);
				tx.lrem("works:in progress", 0, workId);
				if (sync) {
					tx.lpush("works:done:" + workId, workId);
				} else {
					//mettre en id de client
					tx.lpush("works:done", workId);
				}
				tx.exec();
				return;
			} catch (final JedisException e) {
				jedisPool.returnBrokenResource(jedis);
				jedis = null;
			} finally {
				if (jedis != null) {
					jedisPool.returnResource(jedis);
				}
			}
			System.out.println("retry");
			retry++;
		}
		throw new RuntimeException("3 essais ");

	}
}
