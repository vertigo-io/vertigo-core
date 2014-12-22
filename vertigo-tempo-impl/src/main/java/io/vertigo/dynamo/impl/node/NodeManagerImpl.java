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
package io.vertigo.dynamo.impl.node;

import io.vertigo.dynamo.impl.work.worker.local.LocalCoordinator;
import io.vertigo.dynamo.node.Node;
import io.vertigo.dynamo.node.NodeManager;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Implémentation de NodeManager, pour l'execution de travaux par des Workers distant.
 *
 *
 * @author npiedeloup, pchretien
 */
public final class NodeManagerImpl implements NodeManager, Activeable {
	private final List<WorkerPlugin> nodePlugins;
	private final List<Thread> dispatcherThreads = new ArrayList<>();
	private final LocalCoordinator localWorker = new LocalCoordinator(/*workersCount*/5);

	//private final String nodeId;

	@Inject
	public NodeManagerImpl(final List<WorkerPlugin> nodePlugins) {
		Assertion.checkNotNull(nodePlugins);
		//-----
		this.nodePlugins = nodePlugins;
		//---
		for (final WorkerPlugin nodePlugin : this.nodePlugins) {
			for (final String workType : nodePlugin.getWorkTypes()) {
				final WWorker worker = new WWorker(workType, localWorker, nodePlugin);
				dispatcherThreads.add(new Thread(worker));
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<Node> getNodes() {
		return null; //nodes;
	}

	//	private final List<String> workTypes;
	//
	//	@Inject
	//	public WNodePlugin(final String nodeId, final String workTypes) {
	//		Assertion.checkArgNotEmpty(nodeId);
	//		Assertion.checkArgNotEmpty(workTypes);
	//-----
	//		this.nodeId = nodeId;
	//		this.workTypes = Arrays.asList(workTypes.trim().split(";"));
	//	}

	//	public String getNodeId() {
	//		return nodeId;
	//	}

	/** {@inheritDoc} */
	@Override
	public final void start() {
		for (final Thread dispatcherThread : dispatcherThreads) {
			dispatcherThread.start();
		}
	}

	/** {@inheritDoc} */
	@Override
	public final void stop() {
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
	}
}
