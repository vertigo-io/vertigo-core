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

import io.vertigo.core.engines.ElasticaEngine;
import io.vertigo.lang.Activeable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author pchretien
 */
public final class RedisElasticaEngine implements ElasticaEngine, Activeable {
	private static final String HOST = "kasper-redis";
	private final JedisPool jedisPool;
	private Master master;

	private static JedisPool createJedisPool() {
		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		//	jedisPoolConfig.setMaxActive(20);
		return new JedisPool(jedisPoolConfig, HOST);
	}

	public RedisElasticaEngine(/*final boolean server*/) {
		jedisPool = createJedisPool();
	}

	@Override
	public void start() {
		master = new Master(jedisPool);
		master.start();
	}

	@Override
	public void stop() {
		System.out.println("stopping master");
		master.cancelled = true;
		try {
			master.join(100);
		} catch (final InterruptedException e) {
			//
		}
	}

	@Override
	public <F> F createProxy(final Class<F> facadeClass) {
		final InvocationHandler proxy = new RedisInvocationHandler(jedisPool, facadeClass);
		return (F) Proxy.newProxyInstance(proxy.getClass().getClassLoader(), new Class[] { facadeClass }, proxy);
	}

	private static final class Master extends Thread {
		private final ZWorker worker;
		private volatile boolean cancelled;

		Master(final JedisPool jedisPool) {
			System.out.println("master ");
			worker = new ZWorker(jedisPool);
		}

		@Override
		public void run() {
			System.out.print("run master ");
			while (!cancelled) {
				worker.work(1);
				System.out.print(".");
			}
		}
	}

}
