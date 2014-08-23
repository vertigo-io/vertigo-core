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
import io.vertigo.kernel.lang.Option;

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
	private final RedisDB redisDB;
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());

	RedisListenerThread(final RedisDB redisDB) {
		Assertion.checkNotNull(redisDB);
		//-----------------------------------------------------------------
		this.redisDB = redisDB;
	}

	<WR, W> Future<WR> putworkItem(final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		Assertion.checkNotNull(workItem);
		//---------------------------------------------------------------------
		final WFuture<WR> future;
		if (workResultHandler.isDefined()) {
			future = new WFuture<>(workResultHandler.get());
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
			//On attend le résultat (par tranches de 1s)
			final int waitTimeSeconds = 1;
			RedisResult result = redisDB.nextResult(waitTimeSeconds);
			if (result != null){
				final WorkResultHandler workResultHandler = workResultHandlers.get(result.getWorkId());
				if (workResultHandler != null) {
					//Que faire sinon 
					if (result.hasError()){
						workResultHandler.onFailure(result.getError());
					}else{
						workResultHandler.onSuccess(result.getResult());
					}
				}
			}
		}
	}
}
