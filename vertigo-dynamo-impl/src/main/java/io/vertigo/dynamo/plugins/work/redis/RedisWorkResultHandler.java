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

import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

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
		//
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
		try (Jedis jedis = jedisPool.getResource()) {
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
		}
	}
}
