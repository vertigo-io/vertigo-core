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
package io.vertigo.dynamo.plugins.work.redis.worker;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.impl.node.WorkerPlugin;
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
 * NodePlugin
 * Ce plugin permet d'exécuter des travaux en mode distribué.
 * REDIS est utilisé comme plateforme d'échanges.
 *
 * @author pchretien
 */
public final class RedisWorkerPlugin implements WorkerPlugin, Activeable {
	private final List<String> workTypes;
	private final RedisDB redisDB;

	@Inject
	public RedisWorkerPlugin(
			final CodecManager codecManager,
			@Named("nodeId") final String nodeId,
			@Named("workTypes") final String workTypes,
			@Named("host") final String redisHost,
			@Named("port") final int redisPort,
			@Named("timeoutSeconds") final int timeoutSeconds,
			@Named("password") final Option<String> password) {
		Assertion.checkNotNull(codecManager);
		Assertion.checkArgNotEmpty(workTypes);
		Assertion.checkArgNotEmpty(redisHost);
		Assertion.checkArgument(timeoutSeconds < 10000, "Le timeout s'exprime en seconde.");
		//-----
		this.workTypes = Arrays.asList(workTypes.trim().split(";"));
		redisDB = new RedisDB(codecManager, redisHost, redisPort, timeoutSeconds, password);
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getWorkTypes() {
		return workTypes;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		redisDB.start();
		//On enregistre le node
		//redisDB.registerNode(new Node(getNodeId(), true));
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//redisDB.registerNode(new Node(getNodeId(), false));
		redisDB.stop();
	}

	/*public List<Node> getNodes() {
		return redisDB.getNodes();
	}*/

	/** {@inheritDoc} */
	@Override
	public <R, W> WorkItem<R, W> pollWorkItem(final String workType) {
		return redisDB.pollWorkItem(workType);
	}

	/** {@inheritDoc} */
	@Override
	public <R> void putResult(final String workId, final R result, final Throwable error) {
		redisDB.putResult(workId, result, error);
	}

	/** {@inheritDoc} */
	@Override
	public void putStart(final String workId) {
		redisDB.putStart(workId);
	}
}
