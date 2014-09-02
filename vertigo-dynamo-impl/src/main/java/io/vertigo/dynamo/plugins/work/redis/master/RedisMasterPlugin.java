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

import io.vertigo.core.lang.Activeable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.impl.work.MasterPlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.plugins.work.redis.RedisDB;
import io.vertigo.dynamo.plugins.work.redis.RedisDB.RedisResult;
import io.vertigo.dynamo.work.WorkResultHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final List<String> distributedWorkTypes;
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());

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

	private <WR> void setResult(final String workId, final WR result, final Throwable error) {
		Assertion.checkArgNotEmpty(workId);
		Assertion.checkArgument(result == null ^ error == null, "result xor error is null");
		//---------------------------------------------------------------------		
		final WorkResultHandler workResultHandler = workResultHandlers.remove(workId);
		if (workResultHandler != null) {
			//Que faire sinon
			workResultHandler.onDone(result, error);
		}
	}

	private Thread createWatcher() {
		return new Thread() {
			/** {@inheritDoc} */
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					//On attend le résultat (par tranches de 1s)
					final int waitTimeSeconds = 1;
					final RedisResult redisResult = redisDB.pollResult(waitTimeSeconds);
					if (redisResult != null) {
						setResult(redisResult.workId, redisResult.result, redisResult.error);
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
	public <WR, W> void putWorkItem(final WorkItem<WR, W> workItem, final WorkResultHandler<WR> workResultHandler) {
		redisDB.putWorkItem(workItem);
		workResultHandlers.put(workItem.getId(), workResultHandler);
	}
}
