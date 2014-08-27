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

import io.vertigo.dynamo.impl.work.DistributedWorkerPlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.plugins.work.redis.RedisDB;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Ce plugin permet de distribuer des travaux.
 * REDIS est utilisé comme plateforme d'échanges.
 * 
 * @author pchretien
 */
public final class RedisDistributedWorkerPlugin implements DistributedWorkerPlugin, Activeable {
	//	private final int timeoutSeconds;
	private final RedisDB redisDB;
	/*
	 *La map est nécessairement synchronisée.
	 */
	private final RedisQueue redisQueue;
	private final Thread redisQueueWatcher;

	@Inject
	public RedisDistributedWorkerPlugin(final @Named("host") String redisHost, final @Named("port") int redisPort, final @Named("password") Option<String> password, final @Named("timeoutSeconds") int timeoutSeconds) {
		Assertion.checkArgNotEmpty(redisHost);
		//---------------------------------------------------------------------
		redisDB = new RedisDB(redisHost, redisPort, password);
		//		this.timeoutSeconds = timeoutSeconds;
		redisQueue = new RedisQueue(redisDB);
		redisQueueWatcher = new Thread(redisQueue);
	}

	/** {@inheritDoc} */
	public void start() {
		redisDB.start();
		redisQueueWatcher.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		redisQueueWatcher.interrupt();
		try {
			redisQueueWatcher.join();
		} catch (final InterruptedException e) {
			//On ne fait rien
		}
		//---
		redisDB.stop();
	}

	/** {@inheritDoc} */
	public <WR, W> Future<WR> submit(final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		final String workType = obtainWorkType(workItem);
		return redisQueue.submit(workType, workItem, workResultHandler);
	}

	public <WR, W> boolean canProcess(final WorkEngineProvider<WR, W> workEngineProvider) {
		return true;
	}

	private static <WR, W> String obtainWorkType(final WorkItem<WR, W> workItem) {
		//		System.out.println(">>>>" + workItem.getWorkEngineProvider().getName());
		//		return workItem.getWorkEngineProvider().getName();
		return "toto";
	}
}
