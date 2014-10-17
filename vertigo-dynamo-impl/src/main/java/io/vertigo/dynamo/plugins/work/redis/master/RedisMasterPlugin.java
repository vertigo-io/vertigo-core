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

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.impl.work.MasterPlugin;
import io.vertigo.dynamo.impl.work.WorkResult;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.plugins.work.redis.RedisDB;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

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
	private final List<String> distributedWorkTypes;

	@Inject
	public RedisMasterPlugin(final CodecManager codecManager, final @Named("distributedWorkTypes") String distributedWorkTypes, final @Named("host") String redisHost, final @Named("port") int redisPort, final @Named("password") Option<String> password, final @Named("timeoutSeconds") int timeoutSeconds) {
		Assertion.checkArgNotEmpty(distributedWorkTypes);
		Assertion.checkNotNull(codecManager);
		Assertion.checkArgNotEmpty(redisHost);
		//---------------------------------------------------------------------
		this.distributedWorkTypes = Arrays.asList(distributedWorkTypes.split(";"));
		redisDB = new RedisDB(codecManager, redisHost, redisPort, password);
		//		this.timeoutSeconds = timeoutSeconds;
	}

	/** {@inheritDoc} */
	public void start() {
		redisDB.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		redisDB.stop();
	}

	/** {@inheritDoc} */
	@Override
	public List<String> acceptedWorkTypes() {
		return distributedWorkTypes;
	}

	/** {@inheritDoc} */
	public WorkResult pollResult(final int waitTimeSeconds) {
		return redisDB.pollResult(waitTimeSeconds);
	}

	/** {@inheritDoc} */
	public <WR, W> void putWorkItem(final WorkItem<WR, W> workItem) {
		redisDB.putWorkItem(workItem);
	}
}
