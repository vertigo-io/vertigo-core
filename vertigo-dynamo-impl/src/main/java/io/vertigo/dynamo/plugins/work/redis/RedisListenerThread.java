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

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author pchretien
 * $Id: RedisListenerThread.java,v 1.6 2014/01/20 18:56:18 pchretien Exp $
 */
final class RedisListenerThread extends Thread {
	private final JedisPool jedisPool;
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());

	RedisListenerThread(final JedisPool jedisPool) {
		Assertion.checkNotNull(jedisPool);
		//-----------------------------------------------------------------
		this.jedisPool = jedisPool;
	}

	<WR, W> Future<WR> putworkItem(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//---------------------------------------------------------------------
		final WFuture<WR> future;
		if (workItem.getWorkResultHandler().isDefined()) {
			future = new WFuture<>(workItem.getWorkResultHandler().get());
		} else {
			future = new WFuture<>();
		}
		workResultHandlers.put(workItem.getId(), future);
		return future;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		while (!isInterrupted()) {
			//				int retry = 0;
			//				while (retry < 3) {
			try (Jedis jedis = jedisPool.getResource()) {
				//On attend le résultat (par tranches de 1s)
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
			}
		}
	}
}
