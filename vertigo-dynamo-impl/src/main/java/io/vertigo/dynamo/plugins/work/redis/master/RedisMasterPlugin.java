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

import io.vertigo.dynamo.impl.work.MasterPlugin;
import io.vertigo.dynamo.impl.work.WResult;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.plugins.work.redis.RedisDB;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Ce plugin permet de distribuer des travaux.
 * REDIS est utilisé comme plateforme d'échanges.
 * 
 * @author pchretien
 */
public final class RedisMasterPlugin implements MasterPlugin, Activeable {
	private final RedisDB redisDB;
	private Thread redisWatcher;
	private WCallback myCallback;
	private final List<String> distributedWorkTypes;

	@Inject
	public RedisMasterPlugin(final @Named("distributedWorkTypes") String distributedWorkTypes, final @Named("host") String redisHost, final @Named("port") int redisPort, final @Named("password") Option<String> password, final @Named("timeoutSeconds") int timeoutSeconds) {
		Assertion.checkArgNotEmpty(distributedWorkTypes);
		Assertion.checkArgNotEmpty(redisHost);
		//---------------------------------------------------------------------
		this.distributedWorkTypes = Arrays.asList(distributedWorkTypes.split(";"));
		redisDB = new RedisDB(redisHost, redisPort, password);
		//		this.timeoutSeconds = timeoutSeconds;
	}

	/** {@inheritDoc} */
	public List<String> acceptedWorkTypes() {
		return distributedWorkTypes;
	}

	private Thread createWatcher() {
		return new Thread() {
			/** {@inheritDoc} */
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					//On attend le résultat (par tranches de 1s)
					final int waitTimeSeconds = 1;
					final WResult result = pollResult(waitTimeSeconds);
					if (result != null) {
						myCallback.setResult(result);
					}
				}
			}
		};
	}

	/** {@inheritDoc} */
	public void start() {
		redisWatcher = createWatcher();

		redisDB.start();
		redisWatcher.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		if (redisWatcher != null) {
			redisWatcher.interrupt();
			try {
				redisWatcher.join();
			} catch (final InterruptedException e) {
				//On ne fait rien
			}
		}
		//---
		redisDB.stop();
	}

	/** {@inheritDoc} */
	public <WR, W> void putWorkItem(final WorkItem<WR, W> workItem) {
		redisDB.putWorkItem(workItem);
	}

	private WResult<Object> pollResult(final int waitTimeSeconds) {
		return redisDB.pollResult(waitTimeSeconds);
	}

	public <WR> void registerCallback(final WCallback callback) {
		this.myCallback = callback;
	}
}
