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
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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
		try (Jedis jedis = jedisPool.getResource()) {
			final String workId = jedis.brpoplpush("works:todo", "works:in progress", 1);
			if (workId != null) {
				execute(workId, jedis);
			}
			//Cela signifie que l'a rien reçu pendant le brpop
		}
	}

	private <WR, W> void execute(final String workId, final Jedis jedis) {
		final WorkItem<WR, W> workItem = RedisDBUtil.readWorkItem(jedis, workId);
		final Option<WorkResultHandler<WR>> workResultHandler = Option.<WorkResultHandler<WR>> some(new RedisWorkResultHandler<WR>(workId, jedisPool));
		//---Et on fait executer par le workerLocal
		localWorker.submit(workItem, workResultHandler);
	}

}
