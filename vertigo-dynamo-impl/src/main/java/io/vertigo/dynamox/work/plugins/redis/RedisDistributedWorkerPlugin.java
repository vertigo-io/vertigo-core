package io.vertigo.dynamox.work.plugins.redis;

import io.vertigo.dynamo.impl.work.DistributedWorkerPlugin;
import io.vertigo.dynamo.impl.work.worker.local.WorkItem;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.DateUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Ce plugin permet de distribuer des travaux.
 * REDIS est utilis� comme plateforme d'�changes.
 * 
 * @author pchretien
 * $Id: RedisDistributedWorkerPlugin.java,v 1.11 2014/06/26 12:30:08 npiedeloup Exp $
 */
public final class RedisDistributedWorkerPlugin implements DistributedWorkerPlugin, Activeable {
	private final int timeoutSeconds;
	private final JedisPool jedisPool;
	/*
	 *La map est n�cessairement synchronis�e. 
	 */
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());
	private final Thread redisListenerThread;

	@Inject
	public RedisDistributedWorkerPlugin(final @Named("host") String redisHost, final @Named("timeoutSeconds") int timeoutSeconds) {
		Assertion.checkArgNotEmpty(redisHost);
		//---------------------------------------------------------------------
		jedisPool = RedisUtil.createJedisPool(redisHost, 6379);
		this.timeoutSeconds = timeoutSeconds;
		redisListenerThread = new RedisListenerThread(jedisPool, workResultHandlers);
	}

	/** {@inheritDoc} */
	public void start() {
		redisListenerThread.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		redisListenerThread.interrupt();
		try {
			redisListenerThread.join();
		} catch (final InterruptedException e) {
			//On ne fait rien
		}
	}

	/** {@inheritDoc} */
	public <WR, W> WR process(final W work, final WorkEngineProvider<WR, W> workEngineProvider) {
		int retry = 0;
		while (retry < 3) {
			Jedis jedis = jedisPool.getResource();
			try {
				return this.<WR, W> doProcess(jedis, work, workEngineProvider);
			} catch (final JedisException e) {
				jedisPool.returnBrokenResource(jedis);
				jedis = null;
			} finally {
				if (jedis != null) {
					jedisPool.returnResource(jedis);
				}
			}
			//System.out.println("retry");
			retry++;
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				//
			}
		}
		throw new RuntimeException("3 essais ");
	}

	private Object buildResult(final Jedis jedis, final String workId) {
		final Transaction tx = jedis.multi();

		final Response<String> status = tx.hget("work:" + workId, "status");
		final Response<String> result = tx.hget("work:" + workId, "result");
		final Response<String> error = tx.hget("work:" + workId, "error");
		tx.lrem("works:completed", 0, workId);
		tx.del("work:" + workId);

		tx.exec();
		if ("ok".equals(status.get())) {
			//Seul cas ou on remonte un r�sultat
			return RedisUtil.decode(result.get());
		}

		final Throwable t = (Throwable) RedisUtil.decode(error.get());

		//si il ya une erreur 
		if (t instanceof Error) {
			throw Error.class.cast(t);
		}
		if (t instanceof RuntimeException) {
			throw RuntimeException.class.cast(t);
		}
		throw new VRuntimeException(t);

	}

	private <WR, W> WR doProcess(final Jedis jedis, final W work, final WorkEngineProvider<WR, W> workEngineProvider) {
		final String workId = UUID.randomUUID().toString();

		//On renseigne la demande de travaux
		push(jedis, workId, work, workEngineProvider, true);

		//On attend le r�sultat
		//final String id = jedis.brpop(timeoutSeconds, "works:done:" + workId);
		final String id = jedis.brpoplpush("works:done:" + workId, "works:completed", timeoutSeconds);

		if (id == null) {
			throw new VRuntimeException("TimeOut survenu pour work[{0}], dur�e maximale: {1}s", null, workId, timeoutSeconds);
		} else if (!workId.toString().equals(id)) {
			throw new IllegalStateException("Id non coh�renents attendu '" + workId + "' trouv� '" + id + "'");
		}

		return (WR) buildResult(jedis, workId.toString());
	}

	/** {@inheritDoc} */
	public <WR, W> void schedule(final WorkItem<WR, W> workItem) {
		int retry = 0;
		while (retry < 3) {
			Jedis jedis = jedisPool.getResource();
			try {
				doSchedule(jedis, workItem);
				return;
			} catch (final JedisException e) {
				jedisPool.returnBrokenResource(jedis);
				jedis = null;
			} finally {
				if (jedis != null) {
					jedisPool.returnResource(jedis);
				}
			}
			//System.out.println("retry");
			retry++;
		}
		throw new RuntimeException("3 essais ");

	}

	private <WR, W> void doSchedule(final Jedis jedis, final WorkItem<WR, W> workItem) {
		final String workId = UUID.randomUUID().toString();
		push(jedis, workId, workItem.getWork(), workItem.getWorkEngineProvider(), false);

		workResultHandlers.put(workId, workItem.getWorkResultHandler());
	}

	private static <WR, W> void push(final Jedis jedis, final String workId, final W work, final WorkEngineProvider<WR, W> workEngineProvider, final boolean sync) {
		//out.println("creating work [" + workId + "] : " + work.getClass().getSimpleName());

		final Map<String, String> datas = new HashMap<>();
		datas.put("work64", RedisUtil.encode(work));
		datas.put("provider64", RedisUtil.encode(workEngineProvider.getName()));
		datas.put("date", DateUtil.newDate().toString());
		datas.put("sync", Boolean.toString(sync));

		final Transaction tx = jedis.multi();

		tx.hmset("work:" + workId, datas);

		//tx.expire("work:" + workId, 70);
		//On publie la demande de travaux
		tx.lpush("works:todo", workId);

		tx.exec();
	}

	public <WR, W> boolean canProcess(final WorkEngineProvider<WR, W> workEngineProvider) {
		return true;
	}

}
