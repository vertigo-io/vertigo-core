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
package io.vertigo.dynamo.plugins.work.redis.master;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.plugins.work.WFuture;
import io.vertigo.dynamo.plugins.work.WResult;
import io.vertigo.dynamo.plugins.work.redis.RedisDB;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author pchretien
 */
public final class RedisQueue implements Runnable {
	private final RedisDB redisDB;
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());

	RedisQueue(final RedisDB redisDB) {
		Assertion.checkNotNull(redisDB);
		//-----------------------------------------------------------------
		this.redisDB = redisDB;
	}

	//------------A unifier avec restQueue
	<WR, W> Future<WR> submit(final String workType, final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		//1. On renseigne la demande de travaux sur le server redis
		putWorkItem(workType, workItem);
		//2. On attend les notifs sur un thread séparé, la main est rendue de suite
		return createFuture(workItem.getId(), workResultHandler);
	}

	private <WR, W> Future<WR> createFuture(final String workId, final Option<WorkResultHandler<WR>> workResultHandler) {
		Assertion.checkNotNull(workId);
		//---------------------------------------------------------------------
		final WFuture<WR> future;
		if (workResultHandler.isDefined()) {
			future = new WFuture<>(workResultHandler.get());
		} else {
			future = new WFuture<>();
		}
		workResultHandlers.put(workId, future);
		return future;
	}

	private void setResult(final WResult result) {
		final WorkResultHandler workResultHandler = workResultHandlers.remove(result.getWorkId());
		if (workResultHandler != null) {
			//Que faire sinon
			workResultHandler.onDone(result.hasSucceeded(), result.getResult(), result.getError());
		}
	}

	//------------/A unifier avec restQueue

	/** {@inheritDoc} */
	public void run() {
		while (!Thread.interrupted()) {
			//On attend le résultat (par tranches de 1s)
			final int waitTimeSeconds = 1;
			final WResult result = pollResult(waitTimeSeconds);
			if (result != null) {
				setResult(result);
			}
		}
	}

	private <WR, W> void putWorkItem(final String workType, final WorkItem<WR, W> workItem) {
		redisDB.putWorkItem(workType, workItem);
	}

	private WResult<Object> pollResult(final int waitTimeSeconds) {
		return redisDB.pollResult(waitTimeSeconds);
	}

}
