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

import io.vertigo.dynamo.impl.node.NodePlugin;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.node.Node;
import io.vertigo.dynamo.plugins.work.redis.RedisDB;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.ArrayList;
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
public final class RedisNodePlugin implements NodePlugin, Activeable {
	private final RedisDB redisDB;
	private final LocalWorker localWorker = new LocalWorker(/*workersCount*/5);
	private final List<Thread> dispatcherThreads = new ArrayList<>();
	private final String nodeId;
	private final List<String> workTypes;

	@Inject
	public RedisNodePlugin(final @Named("nodeId") String nodeId, final @Named("workTypes") String workTypes, final @Named("host") String redisHost, final @Named("port") int redisPort, final @Named("password") Option<String> password) {
		Assertion.checkArgNotEmpty(nodeId);
		Assertion.checkArgNotEmpty(redisHost);
		Assertion.checkArgNotEmpty(workTypes);
		//---------------------------------------------------------------------
		this.nodeId = nodeId;
		redisDB = new RedisDB(redisHost, redisPort, password);
		//System.out.println("RedisNodePlugin");
		this.workTypes = Arrays.asList(workTypes.trim().split(";"));
		//---
		for (final String workType : this.workTypes) {
			dispatcherThreads.add(new Thread(new RedisDispatcher(nodeId, workType, redisDB, localWorker)));
		}
	}

	/** {@inheritDoc} */
	public void start() {
		//System.out.println("start node");
		redisDB.start();
		//On enregistre le node
		redisDB.registerNode(new Node(nodeId, true));

		for (final Thread thread : dispatcherThreads) {
			thread.start();
		}
	}

	/** {@inheritDoc} */
	public void stop() {
		for (final Thread dispatcherThread : dispatcherThreads) {
			dispatcherThread.interrupt();
		}
		for (final Thread dispatcherThread : dispatcherThreads) {
			try {
				dispatcherThread.join();
			} catch (final InterruptedException e) {
				//On ne fait rien
			}
		}
		localWorker.close();
		redisDB.registerNode(new Node(nodeId, false));
		redisDB.stop();
	}

	//------------------------------------
	//------------------------------------
	//------------------------------------
	//------------------------------------

	/** {@inheritDoc} */
	public List<Node> getNodes() {
		return redisDB.getNodes();
	}

}
