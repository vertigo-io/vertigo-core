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
package io.vertigoimpl.engines.elastica.redis;

import io.vertigo.lang.Assertion;
import io.vertigo.util.DateUtil;
import io.vertigo.util.StringUtil;

import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public final class ZClientWork /*implements Runnable*/{
	private final JedisPool jedisPool;

	//	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<>());

	ZClientWork(final JedisPool jedisPool) {
		Assertion.checkNotNull(jedisPool);
		//-----------
		this.jedisPool = jedisPool;
	}

	//	public <WR, W extends Work<WR, W>> void schedule(final W work, final WorkResultHandler<WR> workResultHandler, final int timeoutSeconds) {
	//		final Jedis jedis = jedisPool.getResource();
	//		try {
	//			doSchedule(jedis, work, workResultHandler, timeoutSeconds);
	//		} catch (final Exception e) {
	//			jedisPool.returnBrokenResource(jedis);
	//			throw new KRuntimeException(e);
	//		} finally {
	//			jedisPool.returnResource(jedis);
	//		}
	//	}
	//
	//	private <WR, W extends Work<WR, W>> WorkResultHandler<WR> doSchedule(final Jedis jedis, final W work, final WorkResultHandler<WR> workResultHandler, final int timeoutSeconds) {
	//		final String workId = publish(jedis, work, false);
	//		workResultHandlers.put(workId, workResultHandler);
	//		return workResultHandler;
	//	}

	public Object process(final ZMethod work, final int timeoutSeconds) {
		try (final Jedis jedis = jedisPool.getResource()) {
			return doProcess(jedis, work, timeoutSeconds);
		}
	}

	private static Object doProcess(final Jedis jedis, final ZMethod work, final int timeoutSeconds) {
		//On renseigne la demande de travaux
		final String id = publish(jedis, work, true);

		//On attend le résultat
		final String workId = jedis.brpoplpush("works:done:" + id, "works:completed", timeoutSeconds);
		if (workId == null) {
			throw new RuntimeException(StringUtil.format("TimeOut survenu pour {0}, durée maximale: {1}", id, timeoutSeconds));
		}
		if (!workId.equals(id)) {
			throw new IllegalStateException("Id non cohérenents attendu '" + id + "' trouvé '" + workId + "'");
		}
		if ("ok".equals(jedis.hget("work:" + id, "status"))) {
			return Util.decodeResult(jedis.hget("work:" + id, "result"));
		}
		final Throwable t = Util.decodeThrowable(jedis.hget("work:" + id, "error"));
		if (t instanceof RuntimeException) {
			return t;
		}
		throw new RuntimeException(t);
	}

	private static String publish(final Jedis jedis, final ZMethod method, final boolean sync) {
		final UUID uuid = UUID.randomUUID();
		final String workId = uuid.toString();

		//out.println("creating work [" + workId + "] : " + work.getClass().getSimpleName());
		jedis.hset("work:" + workId, "base64", Util.encodeMethod(method));
		jedis.hset("work:" + workId, "date", DateUtil.newDate().toString());
		jedis.hset("work:" + workId, "sync", Boolean.toString(sync));
		//On publie la demande de travaux
		jedis.lpush("works:todo", workId);
		return workId;
	}
	//
	//	public void run() {
	//		final Jedis jedis = jedisPool.getResource();
	//		try {
	//			while (true) {
	//				//On attend le résultat
	//				final String workId = jedis.brpoplpush("works:done", "works:completed", 60);
	//				if (workId != null) {
	//					final Object result = Util.decode(jedis.hget("work:" + workId, "result"));
	//					workResultHandlers.get(workId).onSuccess(result);
	//				}
	//			}
	//		} finally {
	//			jedisPool.returnResource(jedis);
	//		}
	//	}
}
