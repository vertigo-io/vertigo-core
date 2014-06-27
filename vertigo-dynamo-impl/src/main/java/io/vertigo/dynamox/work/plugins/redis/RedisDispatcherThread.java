package io.vertigo.dynamox.work.plugins.redis;

import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.impl.work.worker.local.WorkItem;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.kernel.lang.Assertion;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author pchretien
 * $Id: RedisDispatcherThread.java,v 1.8 2014/02/03 17:28:45 pchretien Exp $
 */
final class RedisDispatcherThread extends Thread {
	private final JedisPool jedisPool;
	//	private final String nodeId;
	private final LocalWorker localWorker;

	RedisDispatcherThread(final String nodeId, final JedisPool jedisPool, final LocalWorker localWorker) {
		Assertion.checkArgNotEmpty(nodeId);
		Assertion.checkNotNull(jedisPool);
		Assertion.checkNotNull(localWorker);
		//-----------------------------------------------------------------
		this.jedisPool = jedisPool;
		//	this.nodeId = nodeId;
		this.localWorker = localWorker;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		while (!isInterrupted()) {
			doRun();
		}
	}

	private void doRun() {
		Jedis jedis = jedisPool.getResource();
		try {
			final String workId = jedis.brpoplpush("works:todo", "works:in progress", 1);
			if (workId != null) {
				execute(workId, jedis);
			} else {
				//Cel� signifie que l'a rien re�u pendant 10s
				//out.println(" Worker [" + id + "]waiting....");
			}
			return;
		} catch (final JedisException e) {
			jedisPool.returnBrokenResource(jedis);
			jedis = null;
		} finally {
			if (jedis != null) {
				jedisPool.returnResource(jedis);
			}
		}
	}

	private <WR, W> void execute(final String workId, final Jedis jedis) {
		WorkItem<WR, W> workItem = getWorkItem(workId, jedis);
		localWorker.schedule(workItem);
	}

	private <W, WR> WorkItem<WR, W> getWorkItem(final String workId, final Jedis jedis) {
		//		datas.put("work64", RedisUtil.encode(work));
		//		datas.put("provider64", RedisUtil.encode(workEngineProvider.getName()));
		final Transaction tx = jedis.multi();

		final Response<String> swork = tx.hget("work:" + workId, "work64");
		final Response<String> sname = tx.hget("work:" + workId, "provider64");
		final Response<String> ssync = tx.hget("work:" + workId, "sync");
		tx.exec();

		final W work = (W) RedisUtil.decode(swork.get());
		final String name = (String) RedisUtil.decode(sname.get());
		final WorkEngineProvider<WR, W> workEngineProvider = new WorkEngineProvider<>(name);
		final boolean sync = "true".equals(ssync.get());
		return new WorkItem<>(work, workEngineProvider, new RedisWorkResultHandler<WR>(workId, jedisPool, sync));
	}
}
