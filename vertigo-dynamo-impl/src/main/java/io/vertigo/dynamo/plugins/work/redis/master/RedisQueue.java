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
import io.vertigo.dynamo.plugins.work.WResult;
import io.vertigo.dynamo.plugins.work.master.WQueue;
import io.vertigo.dynamo.plugins.work.redis.RedisDB;
import io.vertigo.kernel.lang.Assertion;

/**
 * @author pchretien
 */
final  class RedisQueue extends WQueue implements Runnable {
	private final RedisDB redisDB;

	RedisQueue(final RedisDB redisDB) {
		Assertion.checkNotNull(redisDB);
		//-----------------------------------------------------------------
		this.redisDB = redisDB;
	}

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

	@Override
	protected <WR, W> void putWorkItem(final String workType, final WorkItem<WR, W> workItem) {
		redisDB.putWorkItem(workType, workItem);
	}

	protected WResult<Object> pollResult(final int waitTimeSeconds) {
		return redisDB.pollResult(waitTimeSeconds);
	}

	@Override
	public <WR, W> WorkItem<WR, W> pollWorkItem(final String workType, final int timeoutInSeconds) {
		return redisDB.pollWorkItem(workType, timeoutInSeconds);
	}

}
