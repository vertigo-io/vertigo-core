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
import io.vertigo.dynamo.impl.work.WResult;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
final class WorkQueueRestServer {
	//pas besoin de synchronized la map, car le obtain est le seul accès et est synchronized
	private final Map<String, BlockingQueue<WorkItem<?, ?>>> workQueueMap = new HashMap<>();
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());

	private static final Logger LOG = Logger.getLogger(WorkQueueRestServer.class);

	//On conserve l'état des work en cours, afin de pouvoir les relancer si besoin (avec un autre uuid)
	//	private final ConcurrentMap<UUID, RunningWorkInfos> runningWorkInfosMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, NodeState> knownNodes = new ConcurrentHashMap<>();
	private final Set<String> activeWorkTypes = Collections.synchronizedSet(new HashSet<String>());
	//	private final Timer checkTimeOutTimer = new Timer("WorkQueueRestServerTimeoutCheck", true);
	private final CodecManager codecManager;

	//	private final long nodeTimeOut;

	/**
	 * Constructeur.
	 * @param queue MultipleWorkQueues
	 * @param nodeTimeOut Timeout avant de considérer un noeud comme mort
	 * @param codecManager Manager de codec
	 */
	public WorkQueueRestServer(final long nodeTimeOut, final CodecManager codecManager) {
		Assertion.checkNotNull(codecManager);
		//---------------------------------------------------------------------
		//	this.nodeTimeOut = nodeTimeOut;
		this.codecManager = codecManager;
	}

	//
	//	/**
	//	 * Démarrage du serveur.
	//	 */
	//	public void start() {
	//		//On lance le démon qui détecte les noeuds morts
	//		//checkTimeOutTimer.scheduleAtFixedRate(new DeadNodeDetectorTask(multipleWorkQueues, nodeTimeOut, knownNodes, runningWorkInfosMap), 10 * 1000, 10 * 1000);
	//	}
	//
	//	/**
	//	 * Arret du serveur.
	//	 */
	//	public void stop() {
	//		checkTimeOutTimer.cancel();
	//	}
	private void setResult(final WResult result) {
		final WorkResultHandler workResultHandler = workResultHandlers.remove(result.getWorkId());
		if (workResultHandler != null) {
			//Que faire sinon
			workResultHandler.onDone(result.hasSucceeded(), result.getResult(), result.getError());
		}
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
		activeWorkTypes.add(nodeWorkType);
	}

	String pollWork(final String workType, final String nodeId) {
		//---------------------------------------------------------------------
		touchNode(nodeId, workType);
		final WorkItem workItem = pollWorkItem(workType, 10);
		final String json;
		if (workItem != null) {
			//			final UUID uuid = UUID.randomUUID();
			//			runningWorkInfosMap.put(uuid, new RunningWorkInfos(workType, workItem, nodeId));
			final byte[] serializedWorkItem = codecManager.getCompressedSerializationCodec().encode((Serializable) workItem.getWork());
			final String base64WorkItem = codecManager.getBase64Codec().encode(serializedWorkItem);
			final String[] sendPack = { workItem.getId(), base64WorkItem };
			json = new Gson().toJson(sendPack, String[].class);
			LOG.info("pollWork(" + workType + ") : 1 Work");
		} else {
			json = ""; //vide si pas de tache en attente
			LOG.info("pollWork(" + workType + ") : no Work");
		}
		return json;
	}

	void onStart(final String workId) {
		LOG.info("onStart(" + workId + ")");
		//---------------------------------------------------------------------
		//	final RunningWorkInfos runningWorkInfos = runningWorkInfosMap.get(UUID.fromString(uuid));
		//		Assertion.checkNotNull(runningWorkInfos, "Ce travail ({0}) n''est pas connu, ou n''est plus en cours.", uuid);
		//		runningWorkInfos.getWorkResultHandler().onStart();
	}

	void onDone(final boolean success, final String uuid, final String base64Result) {
		LOG.info("onDone " + success + " : (" + uuid + ")");
		//---------------------------------------------------------------------
		//		final RunningWorkInfos runningWorkInfos = runningWorkInfosMap.remove(UUID.fromString(uuid));
		//		Assertion.checkNotNull(runningWorkInfos, "Ce travail ({0}) n''est pas connu, ou n''est plus en cours.", uuid);

		final byte[] serializedResult = codecManager.getBase64Codec().decode(base64Result);
		final Object value = codecManager.getCompressedSerializationCodec().decode(serializedResult);
		final Object result = success ? value : null;
		final Throwable error = (Throwable) (success ? null : value);
		setResult(new WResult(uuid, success, result, error));
		//		runningWorkInfos.getWorkResultHandler().onDone(success, result, error);
	}

	String getVersion() {
		return "1.0.0";
	}

	private WorkItem<?, ?> pollWorkItem(final String workType, final int timeoutInSeconds) {
		try {
			//take attend qu'un élément soit disponible toutes les secondes.
			//Poll attend (1s) qu'un élément soit disponible et sinon renvoit null
			final WorkItem<?, ?> workItem = obtainWorkQueue(workType).poll(timeoutInSeconds, TimeUnit.SECONDS);
			return workItem;
		} catch (final InterruptedException e) {
			//dans le cas d'une interruption on arrête de dépiler
			return null;
		}
	}

	<WR, W> void putWorkItem(final WorkItem<WR, W> workItem, final WorkResultHandler<WR> workResultHandler) {
		putWorkItem(workItem);
		workResultHandlers.put(workItem.getId(), workResultHandler);
	}

	/**
	 * Ajoute un travail à faire.
	 * @param <WR> Type du résultat
	 * @param <W> Travail à effectué
	 * @param workType Type du travail
	 * @param workItem Work et WorkResultHandler
	 */
	private <WR, W> void putWorkItem(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//-------------------------------------------------------------------
		try {
			obtainWorkQueue(workItem.getWorkType()).put(workItem);
		} catch (final InterruptedException e) {
			//dans le cas d'une interruption on interdit d'empiler de nouveaux Works
			throw new RuntimeException("putWorkItem", e);
		}
	}

	private synchronized BlockingQueue<WorkItem<?, ?>> obtainWorkQueue(final String workType) {
		BlockingQueue<WorkItem<?, ?>> workQueue = workQueueMap.get(workType);
		if (workQueue == null) {
			workQueue = new LinkedBlockingQueue<>();
			workQueueMap.put(workType, workQueue);
		}
		return workQueue;
	}
}
