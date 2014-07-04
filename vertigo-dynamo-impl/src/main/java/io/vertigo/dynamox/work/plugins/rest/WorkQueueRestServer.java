package io.vertigo.dynamox.work.plugins.rest;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * WorkQueue distribu�e - partie serveur en REST.
 * @author npiedeloup
 * @version $Id: WorkQueueRestServer.java,v 1.12 2014/02/27 10:31:19 pchretien Exp $
 */
final class WorkQueueRestServer {
	private static final Logger LOG = Logger.getLogger(WorkQueueRestServer.class);

	//On conserve l'�tat des work en cours, afin de pouvoir les relancer si besoin (avec un autre uuid)
	private final ConcurrentMap<UUID, RunningWorkInfos> runningWorkInfosMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, NodeState> knownNodes = new ConcurrentHashMap<>();
	private final Set<String> activeWorkTypes = Collections.synchronizedSet(new HashSet<String>());
	private final Timer checkTimeOutTimer = new Timer("WorkQueueRestServerTimeoutCheck", true);
	private final MultipleWorkQueues multipleWorkQueues;
	private final CodecManager codecManager;
	private final long nodeTimeOut;

	/**
	 * Constructeur.
	 * @param multipleWorkQueues MultipleWorkQueues
	 * @param nodeTimeOut Timeout avant de consid�rer un noeud comme mort
	 * @param codecManager Manager de codec
	 */
	public WorkQueueRestServer(final MultipleWorkQueues multipleWorkQueues, final long nodeTimeOut, final CodecManager codecManager) {
		Assertion.checkNotNull(multipleWorkQueues);
		//---------------------------------------------------------------------
		this.multipleWorkQueues = multipleWorkQueues;
		this.nodeTimeOut = nodeTimeOut;
		this.codecManager = codecManager;
	}

	/**
	 * D�marrage du serveur.
	 */
	public void start() {
		//On lance le d�mon qui d�tecte les noeuds morts
		checkTimeOutTimer.scheduleAtFixedRate(new DeadNodeDetectorTask(multipleWorkQueues, nodeTimeOut, knownNodes, runningWorkInfosMap), 10 * 1000, 10 * 1000);
	}

	/**
	 * Arret du serveur.
	 */
	public void stop() {
		checkTimeOutTimer.cancel();
	}

	/**
	 * Signalement de vie d'un node, avec le type de work qu'il annonce. 
	 * Le type de work annonc�, vient compl�ter les pr�c�dents.
	 * @param nodeUID UID du node
	 * @param nodeWorkType Type de work trait�
	 */
	private void touchNode(final String nodeUID, final String nodeWorkType) {
		final NodeState nodeState = knownNodes.putIfAbsent(nodeUID, new NodeState(nodeUID, nodeWorkType));
		if (nodeState != null) {
			nodeState.touch(nodeWorkType);
		}
		activeWorkTypes.add(nodeWorkType);
	}

	public String pollWork(final String workType, final String nodeId) {
		//---------------------------------------------------------------------
		touchNode(nodeId, workType);
		final WorkItem workItem = multipleWorkQueues.pollWorkItem(workType);
		final String json;
		if (workItem != null) {
			final UUID uuid = UUID.randomUUID();
			runningWorkInfosMap.put(uuid, new RunningWorkInfos(workType, workItem, nodeId));
			final byte[] serializedWorkItem = codecManager.getCompressedSerializationCodec().encode((Serializable) workItem.getWork());
			final String base64WorkItem = codecManager.getBase64Codec().encode(serializedWorkItem);
			final String[] sendPack = { uuid.toString(), base64WorkItem };
			json = new Gson().toJson(sendPack, String[].class);
			LOG.info("pollWork(" + workType + ") : 1 Work");
		} else {
			json = ""; //vide si pas de tache en attente
			LOG.info("pollWork(" + workType + ") : no Work");
		}
		return json;
	}

	public void onStart(final String uuid) {
		LOG.info("onStart(" + uuid + ")");
		//---------------------------------------------------------------------
		final RunningWorkInfos runningWorkInfos = runningWorkInfosMap.get(UUID.fromString(uuid));
		Assertion.checkNotNull(runningWorkInfos, "Ce travail ({0}) n''est pas connu, ou n''est plus en cours.", uuid);
		runningWorkInfos.getWorkItem().getWorkResultHandler().get().onStart();
	}

	public void onSuccess(final String uuid, final String base64Result) {
		LOG.info("onSuccess(" + uuid + ")");
		//---------------------------------------------------------------------
		final RunningWorkInfos runningWorkInfos = runningWorkInfosMap.remove(UUID.fromString(uuid));
		Assertion.checkNotNull(runningWorkInfos, "Ce travail ({0}) n''est pas connu, ou n''est plus en cours.", uuid);

		final byte[] serializedResult = codecManager.getBase64Codec().decode(base64Result);
		final Object result = codecManager.getCompressedSerializationCodec().decode(serializedResult);
		runningWorkInfos.getWorkItem().getWorkResultHandler().get().onSuccess(result);
	}

	public void onFailure(final String uuid, final String base64Result) {
		LOG.info("onFailure(" + uuid + ")");
		//---------------------------------------------------------------------
		final RunningWorkInfos runningWorkInfos = runningWorkInfosMap.remove(UUID.fromString(uuid));
		Assertion.checkNotNull(runningWorkInfos, "Ce travail ({0}) n''est pas connu, ou n''est plus en cours.", uuid);

		final byte[] serializedResult = codecManager.getBase64Codec().decode(base64Result);
		final Throwable error = (Throwable) codecManager.getCompressedSerializationCodec().decode(serializedResult);
		runningWorkInfos.getWorkItem().getWorkResultHandler().get().onFailure(error);
	}

	public String getVersion() {
		return "1.0.0";
	}

	private static class RunningWorkInfos {
		private final String workType;
		private final WorkItem workItem;
		private final String nodeUID;

		//private final long startTime;

		public RunningWorkInfos(final String workType, final WorkItem workItem, final String nodeUID) {
			this.workType = workType;
			this.workItem = workItem;
			this.nodeUID = nodeUID;
			//startTime = System.currentTimeMillis();
		}

		public WorkItem<Object, ?> getWorkItem() {
			return workItem;
		}

		public String getWorkType() {
			return workType;
		}

		public String getNodeUID() {
			return nodeUID;
		}

		//		public long getStartTime() {
		//			return startTime;
		//		}
	}

	private static class DeadNodeDetectorTask extends TimerTask {
		private final long deadNodeTimeout;
		private final ConcurrentMap<UUID, RunningWorkInfos> runningWorkInfosMap;
		private final ConcurrentMap<String, NodeState> knownNodes;
		private final MultipleWorkQueues multipleWorkQueues;

		public DeadNodeDetectorTask(final MultipleWorkQueues multipleWorkQueues, final long deadNodeTimeout, final ConcurrentMap<String, NodeState> knownNodes, final ConcurrentMap<UUID, RunningWorkInfos> runningWorkInfosMap) {
			this.deadNodeTimeout = deadNodeTimeout;
			this.runningWorkInfosMap = runningWorkInfosMap;
			this.knownNodes = knownNodes;
			this.multipleWorkQueues = multipleWorkQueues;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			final Set<String> deadNodes = new HashSet<>();
			//Comme d�fini dans le contrat de la ConcurrentMap : l'iterator est weakly consistent : et ne lance pas de ConcurrentModificationException  
			for (final NodeState nodeState : knownNodes.values()) {
				//sans signe de vie depuis deadNodeTimeout, on consid�re le noeud comme mort
				if (System.currentTimeMillis() - nodeState.getLastSeenTime() > deadNodeTimeout) {
					deadNodes.add(nodeState.getNodeUID());
				}
			}
			if (!deadNodes.isEmpty()) {
				LOG.info("Noeuds arr�t�s : " + deadNodes);
				for (final RunningWorkInfos runningWorkInfos : runningWorkInfosMap.values()) {
					if (deadNodes.contains(runningWorkInfos.getNodeUID())) {
						multipleWorkQueues.putWorkItem(runningWorkInfos.getWorkType(), runningWorkInfos.getWorkItem());
					}
				}
			}
		}
	}
}
