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

import io.vertigo.dynamo.impl.work.DistributedWorkerPlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.util.DateUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Named;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Ce plugin permet de distribuer des travaux.
 * REDIS est utilisé comme plateforme d'échanges.
 * 
 * @author pchretien
 * $Id: RedisDistributedWorkerPlugin.java,v 1.11 2014/06/26 12:30:08 npiedeloup Exp $
 */
public final class RedisDistributedWorkerPlugin implements DistributedWorkerPlugin, Activeable {
	private final int timeoutSeconds;
	private final JedisPool jedisPool;
	/*
	 *La map est nécessairement synchronisée. 
	 */
	private final RedisListenerThread redisListenerThread;

	@Inject
	public RedisDistributedWorkerPlugin(final @Named("host") String redisHost, final @Named("port") int redisPort, final @Named("password") Option<String> password, final @Named("timeoutSeconds") int timeoutSeconds) {
		Assertion.checkArgNotEmpty(redisHost);
		//---------------------------------------------------------------------
		jedisPool = RedisUtil.createJedisPool(redisHost, redisPort, password);
		this.timeoutSeconds = timeoutSeconds;
		redisListenerThread = new RedisListenerThread(jedisPool);
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
	public <WR, W> void execute(final WorkItem<WR, W> workItem) {
		int retry = 0;
		while (retry < 3) {
			Jedis jedis = jedisPool.getResource();
			try {
				//---
				if (workItem.isSync()) {
					this.<WR, W> doProcess(jedis, workItem);
				} else {
					this.<WR, W> doSchedule(jedis, workItem);
				}
				//C'est bon on s'arrête 
				return;
				//---
			} catch (final JedisException e) {
				jedisPool.returnBrokenResource(jedis);
				jedis = null;
			} finally {
				jedisPool.returnResource(jedis);
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

	private static Object buildResult(final Jedis jedis, final String workId) {
		final Transaction tx = jedis.multi();

		final Response<String> status = tx.hget("work:" + workId, "status");
		final Response<String> result = tx.hget("work:" + workId, "result");
		final Response<String> error = tx.hget("work:" + workId, "error");
		tx.lrem("works:completed", 0, workId);
		tx.del("work:" + workId);

		tx.exec();
		if ("ok".equals(status.get())) {
			//Seul cas ou on remonte un résultat
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
		throw new RuntimeException(t);

	}

	private <WR, W> void doSchedule(final Jedis jedis, final WorkItem<WR, W> workItem) {
		//1. On renseigne la demande de travaux
		putWorkItem(jedis, workItem);
		//2. On attend les notifs
		redisListenerThread.putworkItem(workItem);
	}

	private <WR, W> void doProcess(final Jedis jedis, final WorkItem<WR, W> workItem) {
		//1. On renseigne la demande de travaux
		putWorkItem(jedis, workItem);
		//2. On attend le résultat
		final WR result = waitResult(jedis, workItem);
		//3. On affecte le résultat
		workItem.setResult(new Future<WR>() {
			public boolean cancel(final boolean mayInterruptIfRunning) {
				return false;
			}

			public boolean isCancelled() {
				return false;
			}

			public boolean isDone() {
				return false;
			}

			public WR get() throws InterruptedException, ExecutionException {
				return result;
			}

			public WR get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				return result;
			}
		});
	}

	private <WR, W> WR waitResult(final Jedis jedis, final WorkItem<WR, W> workItem) {
		//On attend le résultat
		//final String id = jedis.brpop(timeoutSeconds, "works:done:" + workId);
		final String id = jedis.brpoplpush("works:done:" + workItem.getId(), "works:completed", timeoutSeconds);

		if (id == null) {
			throw new RuntimeException("TimeOut survenu pour work[" + workItem.getId() + "], duree maximale: " + timeoutSeconds + " s");
		} else if (!workItem.getId().equals(id)) {
			throw new IllegalStateException("Id non cohérents attendu '" + workItem.getId() + "' trouvé '" + id + "'");
		}
		return (WR) buildResult(jedis, workItem.getId());
	}

	private static <WR, W> void putWorkItem(final Jedis jedis, final WorkItem<WR, W> workItem) {
		//out.println("creating work [" + workId + "] : " + work.getClass().getSimpleName());

		final Map<String, String> datas = new HashMap<>();
		datas.put("work64", RedisUtil.encode(workItem.getWork()));
		datas.put("provider64", RedisUtil.encode(workItem.getWorkEngineProvider().getName()));
		datas.put("date", DateUtil.newDate().toString());

		final Transaction tx = jedis.multi();

		tx.hmset("work:" + workItem.getId(), datas);

		//tx.expire("work:" + workId, 70);
		//On publie la demande de travaux
		tx.lpush("works:todo", workItem.getId());

		tx.exec();
	}

	public <WR, W> boolean canProcess(final WorkEngineProvider<WR, W> workEngineProvider) {
		return true;
	}

}
