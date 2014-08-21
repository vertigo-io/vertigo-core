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
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
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
	//	private final int timeoutSeconds;
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
		//		this.timeoutSeconds = timeoutSeconds;
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
	public <WR, W> Future<WR> submit(final WorkItem<WR, W> workItem) {
		int retry = 0;
		while (retry < 3) {
			Jedis jedis = jedisPool.getResource();
			try {
				return this.<WR, W> doSubmit(jedis, workItem);
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

	private <WR, W> Future<WR> doSubmit(final Jedis jedis, final WorkItem<WR, W> workItem) {
		//1. On renseigne la demande de travaux
		putWorkItem(jedis, workItem);
		//2. On attend les notifs sur un thread séparé pour rendre la main
		return redisListenerThread.putworkItem(workItem);
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
