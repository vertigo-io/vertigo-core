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
package io.vertigo.dynamo.plugins.work.rest.master;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.WorkResult;
import io.vertigo.lang.Assertion;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * WorkQueue distribuée - partie serveur en REST.
 * @author npiedeloup
 */
final class RestQueueServer {
	//pas besoin de synchronized la map, car le obtain est le seul accès et est synchronized
	private final Map<String, BlockingQueue<WaitingWorkInfos>> workQueueMap = new HashMap<>();

	private static final Logger LOG = Logger.getLogger(RestQueueServer.class);

	//On conserve l'état des work en cours, afin de pouvoir les relancer si besoin (avec un autre uuid)
	private final ConcurrentMap<String, RunningWorkInfos> runningWorkInfosMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, NodeState> knownNodes = new ConcurrentHashMap<>();
	private final CodecManager codecManager;
	private final BlockingQueue<WorkResult> resultQueue = new LinkedBlockingQueue<>();

	private final int deadWorkTypeTimeoutSec;
	private final long nodeTimeOutSec;
	private final int pullTimeoutSec;

	/**
	 * Constructeur.
	 * @param nodeTimeOutSec Timeout (secondes) avant de considérer un noeud comme mort
	 * @param codecManager Manager de codec
	 * @param pullTimeoutSec Timeout (secondes) utilisé lors des long pull
	 * @param daemonManager Daemons manager
	 */
	public RestQueueServer(final int nodeTimeOutSec, final CodecManager codecManager, final int pullTimeoutSec, final DaemonManager daemonManager) {
		Assertion.checkNotNull(codecManager);
		//-----
		this.nodeTimeOutSec = nodeTimeOutSec;
		this.pullTimeoutSec = pullTimeoutSec;
		deadWorkTypeTimeoutSec = 60; //by convention : dead workType timeout after 60s
		this.codecManager = codecManager;

		daemonManager.registerDaemon("workQueueTimeoutCheck", DeadNodeDetectorDaemon.class, 10);
	}

	/**
	 * Vérifie les noeuds morts, et si oui remets les workItems dans la pile.
	 */
	void checkDeadNodes() {
		final Set<String> deadNodes = new HashSet<>();
		//Comme défini dans le contrat de la ConcurrentMap : l'iterator est weakly consistent : et ne lance pas de ConcurrentModificationException
		for (final NodeState nodeState : knownNodes.values()) {
			//sans signe de vie depuis deadNodeTimeout, on considère le noeud comme mort
			if (!isActiveNode(nodeState)) {
				deadNodes.add(nodeState.getNodeUID());
			}
		}
		if (!deadNodes.isEmpty()) {
			LOG.warn("Stopped nodes detected : " + deadNodes);
			for (final RunningWorkInfos runningWorkInfos : runningWorkInfosMap.values()) {
				if (deadNodes.contains(runningWorkInfos.getNodeUID())) {
					putWorkItem(runningWorkInfos.getWorkItem());
				}
			}
		}
	}

	/**
	 * Vérifie les WorkItems sur des workTypes inactifs
	 */
	void checkDeadWorkItems() {
		for (final String workType : new ArrayList<>(workQueueMap.keySet())) {
			BlockingQueue<WaitingWorkInfos> workItemQueue;
			try {
				workItemQueue = obtainWorkQueue(workType);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			if (!workItemQueue.isEmpty() && !isActiveWorkType(workType)) {
				//Comme défini dans le contrat de la BlockingQueue : l'iterator est weakly consistent : et ne lance pas de ConcurrentModificationException
				for (final Iterator<WaitingWorkInfos> it = workItemQueue.iterator(); it.hasNext();) {
					final WaitingWorkInfos waitingWorkInfos = it.next();
					if (waitingWorkInfos.getWaitingAge() > deadWorkTypeTimeoutSec * 1000) {
						it.remove();
						LOG.info("waiting timeout (" + waitingWorkInfos.getWorkItem().getId() + ")");
						resultQueue.add(new WorkResult(waitingWorkInfos.getWorkItem().getId(), null, new IOException("Timeout workId " + waitingWorkInfos.getWorkItem().getId() + " after " + deadWorkTypeTimeoutSec + "s : No active node for this workType (" + workType + ")")));
					}
				}
			}
		}
	}

	private boolean isActiveNode(final NodeState nodeState) {
		return System.currentTimeMillis() - nodeState.getLastSeenTime() < nodeTimeOutSec * 1000;
	}

	private boolean isActiveWorkType(final String workType) {
		for (final NodeState nodeState : knownNodes.values()) {
			if (isActiveNode(nodeState) && nodeState.isWorkTypeSupported(workType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Signalement de vie d'un node, avec le type de work qu'il annonce.
	 * Le type de work annoncé, vient compléter les précédents.
	 * @param nodeUID UID du node
	 * @param nodeWorkType Type de work traité
	 */
	private void touchNode(final String nodeUID, final String nodeWorkType) {
		final NodeState nodeState = knownNodes.putIfAbsent(nodeUID, new NodeState(nodeUID, nodeWorkType));
		if (nodeState != null) {
			nodeState.touch(nodeWorkType);
		}
	}

	String pollWork(final String workType, final String nodeId) {
		//-----
		touchNode(nodeId, workType);
		final WorkItem workItem = pollWorkItem(workType, pullTimeoutSec);
		final String json;
		if (workItem != null) {
			runningWorkInfosMap.put(workItem.getId(), new RunningWorkInfos(workItem, nodeId));
			final byte[] serializedWorkItem = codecManager.getCompressedSerializationCodec().encode((Serializable) workItem.getWork());
			final String base64WorkItem = codecManager.getBase64Codec().encode(serializedWorkItem);
			final String[] sendPack = { workItem.getId(), base64WorkItem };
			json = new Gson().toJson(sendPack, String[].class);
			LOG.info("pollWork(" + workType + "," + nodeId + ") : 1 Work");
		} else {
			json = ""; //vide si pas de tache en attente
			LOG.info("pollWork(" + workType + "," + nodeId + ") : no Work");
		}
		return json;
	}

	void onStart(final String workId) {
		LOG.info("onStart(" + workId + ")");
	}

	void onDone(final boolean success, final String workId, final String base64Result) {
		LOG.info("onDone " + success + " : (" + workId + ")");
		//-----
		final RunningWorkInfos runningWorkInfos = runningWorkInfosMap.remove(workId);
		Assertion.checkNotNull(runningWorkInfos, "Ce travail ({0}) n''est pas connu, ou n''est plus en cours.", workId);

		final byte[] serializedResult = codecManager.getBase64Codec().decode(base64Result);
		final Object value = codecManager.getCompressedSerializationCodec().decode(serializedResult);
		final Object result = success ? value : null;
		final Throwable error = (Throwable) (success ? null : value);
		resultQueue.add(new WorkResult(workId, result, error));
	}

	WorkResult pollResult(final int waitTimeSeconds) {
		try {
			return resultQueue.poll(waitTimeSeconds, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	String getApiVersion() {
		return "1.0.0";
	}

	private WorkItem<?, ?> pollWorkItem(final String workType, final int pullTimeoutInSeconds) {
		try {
			//take attend qu'un élément soit disponible toutes les secondes.
			//Poll attend (1s) qu'un élément soit disponible et sinon renvoit null
			final WaitingWorkInfos waitingWorkInfos = obtainWorkQueue(workType).poll(pullTimeoutInSeconds, TimeUnit.SECONDS);
			return waitingWorkInfos != null ? waitingWorkInfos.getWorkItem() : null;
		} catch (final InterruptedException e) {
			//dans le cas d'une interruption on arrête de dépiler
			return null;
		}
	}

	/**
	 * Ajoute un travail à faire.
	 * @param <WR> Type du résultat
	 * @param <W> Travail à effectué
	 * @param workItem Work et WorkResultHandler
	 */
	<WR, W> void putWorkItem(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		if (!isActiveWorkType(workItem.getWorkType())) {
			LOG.warn("No active node for this workType : " + workItem.getWorkType());
		}
		try {
			obtainWorkQueue(workItem.getWorkType()).put(new WaitingWorkInfos(workItem));
		} catch (final InterruptedException e) {
			//dans le cas d'une interruption on interdit d'empiler de nouveaux Works
			throw new RuntimeException("putWorkItem", e);
		}
	}

	private synchronized BlockingQueue<WaitingWorkInfos> obtainWorkQueue(final String workType) throws InterruptedException {
		checkInterrupted();
		BlockingQueue<WaitingWorkInfos> workQueue = workQueueMap.get(workType);
		if (workQueue == null) {
			workQueue = new LinkedBlockingQueue<>();
			workQueueMap.put(workType, workQueue);
		}
		return workQueue;
	}

	private static class WaitingWorkInfos {
		private final WorkItem workItem;
		private final long startWaitingTime;

		public WaitingWorkInfos(final WorkItem workItem) {
			this.workItem = workItem;
			startWaitingTime = System.currentTimeMillis();
		}

		public WorkItem getWorkItem() {
			return workItem;
		}

		public long getWaitingAge() {
			return System.currentTimeMillis() - startWaitingTime;
		}
	}

	private static class RunningWorkInfos {
		private final WorkItem workItem;
		private final String nodeUID;

		public RunningWorkInfos(final WorkItem workItem, final String nodeUID) {
			this.workItem = workItem;
			this.nodeUID = nodeUID;
		}

		public WorkItem getWorkItem() {
			return workItem;
		}

		public String getNodeUID() {
			return nodeUID;
		}
	}

	private static void checkInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Thread interruption required");
		}
	}

	public static class DeadNodeDetectorDaemon implements Daemon {
		private final RestQueueServer restQueueServer;

		/**
		 * @param restQueueServer This queueServer
		 */
		public DeadNodeDetectorDaemon(final RestQueueServer restQueueServer) {
			Assertion.checkNotNull(restQueueServer);
			//------
			this.restQueueServer = restQueueServer;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			restQueueServer.checkDeadNodes();
			restQueueServer.checkDeadWorkItems();
		}
	}

}
