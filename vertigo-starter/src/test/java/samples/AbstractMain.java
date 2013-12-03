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
package samples;

import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.di.configurator.ModuleConfigBuilder;
import io.vertigo.kernel.home.data.BioManager;
import io.vertigo.kernel.home.data.BioManagerImpl;
import io.vertigo.kernel.home.data.MathManager;
import io.vertigo.kernel.home.data.MathManagerImpl;
import io.vertigo.kernel.home.data.MathPlugin;
import io.vertigoimpl.engines.elastica.redis.RedisElasticaEngine;

/**
 * 
 * On cr�e des taches et on lances simultan�ment des workers.
 * 
 * @author pchretien
 */

abstract class AbstractMain {

	//
	//	public static void main(final String[] args) throws Exception {
	//		System.out.println("start ");
	//		final JedisPool jedisPool = createJedisPool();
	//		final HomeConfig homeConfig = createConfig();
	//		Home.start(homeConfig);
	//		try {
	//			new Master(jedisPool).start();
	//			testClient(jedisPool);
	//
	//		} finally {
	//			Home.stop();
	//		}
	//	}
	//
	//	private static void testClient(final JedisPool jedisPool) {
	//		System.out.println("testClient ");
	//		final ElasticaClient elasticaClient = new RedisElasticaClientImpl(jedisPool);
	//		final BioManager bioManager = elasticaClient.createProxy(BioManager.class);
	//		System.out.println("proxy ok ");
	//		final int res = bioManager.add(3, 5);
	//		System.out.println("biores:>>" + res);
	//	//	}
	//	private static final String HOST = "localhost";
	//
	//	protected static JedisPool createJedisPool() {
	//		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
	//		jedisPoolConfig.setMaxActive(20);
	//		return new JedisPool(jedisPoolConfig, HOST);
	//
	//	}
	//	private ElasticaEngine elasticaEngine;
	//
	//	public ElasticaEngine getElasticaEngine() {
	//		return elasticaEngine;
	//	}

	protected final ComponentSpaceConfig createConfig(final RedisElasticaEngine elasticaEngine, final boolean server) {
		if (server) {
			System.out.println("start Server");
		} else {
			System.out.println("start Client");
		}
		// @formatter:off
		final ModuleConfigBuilder moduleConfigBuilder = new ComponentSpaceConfigBuilder().withElasticaEngine(elasticaEngine).withSilence(!server).beginModule("Bio");

		if (server) {
			moduleConfigBuilder.beginComponent(BioManager.class, BioManagerImpl.class).endComponent().beginComponent(MathManager.class, MathManagerImpl.class).withParam("start", "100").beginPlugin(MathPlugin.class).withParam("factor", "20").endPlugin().endComponent();
		} else {
			moduleConfigBuilder.beginElasticComponent(BioManager.class).endComponent();
		}

		final ComponentSpaceConfig componentSpaceConfig = moduleConfigBuilder.endModule().build();
		// @formatter:on
		return componentSpaceConfig;
	}
	//	private static class Master extends Thread {
	//		private final ZWorker worker;
	//
	//		Master(final JedisPool jedisPool) {
	//			System.out.println("master ");
	//			final BioManager bioManager = Home.getManagerSpace().getManager(BioManager.class);
	//			worker = new ZWorker(jedisPool, BioManager.class, bioManager);
	//		}
	//
	//		@Override
	//		public void run() {
	//			System.out.println("run master ");
	//			worker.work(10);
	//			System.out.println("work OK");
	//		}
	//	}
}
//	private static final int WORKERS = 10;
//
//	public static void main(final String[] args) throws Exception {
//		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//		jedisPoolConfig.setMaxActive(20);
//		final JedisPool jedisPool = new JedisPool(jedisPoolConfig, HOST);
//		final ZClientWork clientWork = new ZClientWork(jedisPool);
//		new Thread(clientWork).start();
//
//		reset(jedisPool);
//
//		startWorkers(jedisPool, WORKERS);
//
//		while (true) {
//			if ("roro".length() == 78) {
//				testASync(jedisPool, clientWork);
//			}
//			final long start = System.currentTimeMillis();
//			for (int i = 0; i < 10; i++) {
//				testSync(jedisPool, clientWork, i);
//				//	Thread.sleep(1000); //1s
//			}
//			System.out.println("duree sync : " + (System.currentTimeMillis() - start) + " ms");
//		}
//	}
//
//	private static void reset(final JedisPool jedisPool) {
//		final Jedis jedis = jedisPool.getResource();
//		try {
//			jedis.flushAll();
//		} finally {
//			jedisPool.returnResource(jedis);
//		}
//	}
//
//	private static void testASync(final JedisPool jedisPool, final ZClientWork clientWork) throws InterruptedException {
//		final long start = System.currentTimeMillis();
//		for (int i = 0; i < 100; i++) {
//			final MyWorkResultHanlder<Long> workResultHanlder = new MyWorkResultHanlder<>();
//			clientWork.schedule(new DivideWork(100, 20), workResultHanlder, 1000);
//			final boolean finished = workResultHanlder.waitFinish(1100);
//			Assert.assertTrue("Work non termin� ", finished);
//		}
//		//	waitForAll(workItems, 10);
//		System.out.println("duree async: " + (System.currentTimeMillis() - start) + " ms");
//	}
//
//	private static void testSync(final JedisPool jedisPool, final ZClientWork clientWork, final int i) {
//		//		final long start = System.currentTimeMillis();
//		try {
//			final Object res = clientWork.process(new DivideWork(100, 20), 5);
//			if (i == 1) {
//				System.out.println("res :" + res);
//				//		System.out.println("duree sync [" + i + "] : " + (System.currentTimeMillis() - start) + " ms");
//			}
//		} catch (final Exception e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	private static void startWorkers(final JedisPool jedisPool, final int workers) {
//		for (int i = 0; i < workers; i++) {
//			new Thread(new ZWorker("id:" + i, jedisPool)).start();
//		}
//	}

