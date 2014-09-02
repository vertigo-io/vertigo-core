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

import io.vertigo.core.lang.Assertion;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public final class ZWorker /*implements Runnable*/{
	//	private final Class<? extends Manager> managerClass;
	private final JedisPool jedisPool;

	public ZWorker(final JedisPool jedisPool) {
		Assertion.checkNotNull(jedisPool);
		//-----------
		this.jedisPool = jedisPool;
		//	this.managerClass = managerClass;
	}

	private void execute(final Jedis jedis, final String workId) {
		final String base64 = jedis.hget("work:" + workId, "base64");
		final ZMethod work = Util.decodeMethod(base64);
		final boolean sync = "true".equals(jedis.hget("work:" + workId, "sync"));
		//System.out.println("   - work :" + work.getClass().getSimpleName());
		try {
			final Object result = work.run();
			jedis.hset("work:" + workId, "result", Util.encodeResult(result));
			jedis.hset("work:" + workId, "status", "ok");
		} catch (final Throwable t) {
			jedis.hset("work:" + workId, "status", "ko");
			jedis.hset("work:" + workId, "error", Util.encodeError(t));
		}
		if (sync) {
			jedis.lpush("works:done:" + workId, workId);
		} else {
			//mettre en id de client
			jedis.lpush("works:done", workId);
		}
	}

	public void work(final int time) {
		//		System.out.println("started :");
		//		while (true) {
		doRun(time);
		//		}
	}

	private void doRun(final int time) {
		try (final Jedis jedis = jedisPool.getResource()) {
			final String workId = jedis.brpoplpush("works:todo", "works:doing", 10);
			//System.out.println("todo.size : " + jedis.llen("works:todo"));
			if (workId != null) {
				//	out.println("Worker[" + id + "]executing work [" + workId + "]");
				execute(jedis, workId);
			} else {
				//out.println(" Worker [" + id + "]waiting....");
			}
		}
	}
}
