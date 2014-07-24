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
package io.vertigo.dynamo.plugins.work.rest;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.impl.node.NodePlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.dynamo.impl.work.worker.local.WorkItemExecutor;
import io.vertigo.dynamo.node.Node;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

/**
 * Impl�mentation de DistributedWorkManager, pour l'execution de travaux par des Workers distant.
 * Cette impl�mentation repr�sente la partie client qui se d�ploie en ferme.
 * 1- contacte la partie serveur pour r�cup�rer les travaux qu'elle sait g�rer, 
 * 2- execute la tache en synchrone exclusivement
 * 3- retourne le r�sultat au serveur
 * 
 * @author npiedeloup, pchretien
 * @version $Id: RestDistributedWorkNodePlugin.java,v 1.12 2014/02/03 17:28:45 pchretien Exp $
 */
public final class RestDistributedWorkNodePlugin implements NodePlugin, Activeable {

	/** Log du workerNode. */
	final Logger logger = Logger.getLogger(RestDistributedWorkNodePlugin.class);

	private final List<String> workTypes;

	private final WorkManager workManager;
	private final WorkQueueRestClient workQueueClient; //devrait etre un plugin

	private final String nodeId;

	/**
	 * Constructeur.
	 * @param nodeId Identifiant du noeud
	 * @param workTypes Types de travail g�r�s
	 * @param serverUrl Url du serveur
	 * @param workManager Manager des works
	 * @param codecManager Manager d'encodage/decodage
	 */
	@Inject
	public RestDistributedWorkNodePlugin(@Named("nodeId") final String nodeId, @Named("workTypes") final String workTypes, @Named("serverUrl") final String serverUrl, final WorkManager workManager, final CodecManager codecManager) {
		Assertion.checkArgNotEmpty(nodeId);
		Assertion.checkArgNotEmpty(workTypes);
		Assertion.checkArgNotEmpty(serverUrl);
		Assertion.checkNotNull(workManager);
		//---------------------------------------------------------------------
		this.nodeId = nodeId;
		this.workTypes = Arrays.asList(workTypes.trim().split(";"));
		this.workManager = workManager;
		workQueueClient = new WorkQueueRestClient(nodeId, serverUrl + "/workQueue", codecManager);
	}

	/** {@inheritDoc} */
	public void start() {
		//1 pooler par type, pour �viter que l'attente lors du poll pour une file vide p�nalise les autres
		//de plus le traitment lui m�me sera synchrone pour que l'on ne r�cup�re un autre Work qu'apr�s avoir finit le premier
		for (final String workType : workTypes) {
			final Callable<Void> task = new PollWorkTask(workType, workQueueClient);
			workManager.schedule(task, new ReScheduleWorkResultHandler(task, 25, workManager));//Le WorkResult permet de toujours poller les workItem, m�me en cas d'erreur.
			logger.info(">>>>>> PollWorksTask " + workType + " Started");
			System.out.println(">>>>>> PollWorksTask " + workType + " Started");
		}
	}

	/** {@inheritDoc} */
	public void stop() {
		//rien
	}

	//-------------------------------------------------------------------------
	// Methodes appelant la workQueue distribu�e.
	// TODO: utiliser un plugin
	//-------------------------------------------------------------------------

	/**
	 * Tache runnable permettant l'ex�cution d'un travail.
	 * @author npiedeloup
	 * @version $Id: RestDistributedWorkNodePlugin.java,v 1.12 2014/02/03 17:28:45 pchretien Exp $
	 */
	private static final class PollWorkTask implements Callable<Void> {
		private final WorkQueueRestClient workQueueClient;
		private final String workType;
		private final Worker localSyncWorker = new LocalSyncWorker();

		/**
		 * Constructeur.
		 * @param workType Type de work
		 * @param workQueueClient Client REST
		 */
		public PollWorkTask(final String workType, final WorkQueueRestClient workQueueClient) {
			Assertion.checkArgNotEmpty(workType);
			Assertion.checkNotNull(workQueueClient);
			//---------------------------------------------------------------------
			this.workType = workType;
			this.workQueueClient = workQueueClient;
		}

		/** {@inheritDoc} */
		public Void call() throws Exception {
			final WorkItem nextWorkItem = workQueueClient.pollWorkItem(workType);
			if (nextWorkItem != null) {
				//On rerentre dans le WorkItemExecutor pour traiter le travail
				//Le workResultHandler sait d�j� r�pondre au serveur pour l'avancement du traitement
				final WorkItemExecutor workItemExecutor = new WorkItemExecutor(localSyncWorker, nextWorkItem);
				workItemExecutor.run();
			}
			return null;
		}
	}

	private static final class LocalSyncWorker implements Worker {
		/** {@inheritDoc} */
		public <WR, W> void process(final WorkItem<WR, W> workItem) {
			workItem.setResult(workItem.getWorkEngineProvider().provide().process(workItem.getWork()));
		}

		/** {@inheritDoc} */
		public <WR, W> void schedule(final WorkItem<WR, W> workItem) {
			throw new UnsupportedOperationException("Non g�r�, uniquement synchrone");
		}
	}

	private static final class ReScheduleWorkResultHandler implements WorkResultHandler<Void> {
		private final Callable<Void> task;
		private final long pauseMs;
		private final WorkManager workManager;

		public ReScheduleWorkResultHandler(final Callable<Void> task, final long pauseMs, final WorkManager workManager) {
			Assertion.checkNotNull(task);
			Assertion.checkArgument(pauseMs >= 0 && pauseMs < 1000000, "La pause est exprim� en millisecond et est >=0 et < 1000000");
			Assertion.checkNotNull(workManager);
			//-----------------------------------------------------------------
			this.task = task;
			this.pauseMs = pauseMs;
			this.workManager = workManager;
		}

		/** {@inheritDoc} */
		public void onStart() {
			//rien
		}

		/** {@inheritDoc} */
		public void onSuccess(final Void result) {
			reSchedule();
		}

		/** {@inheritDoc} */
		public void onFailure(final Throwable error) {
			reSchedule();
		}

		private void reSchedule() {
			try {
				Thread.sleep(pauseMs);
				workManager.schedule(task, this);
			} catch (final InterruptedException e) {
				//rien on stop
			}
		}
	}

	/** {@inheritDoc} */
	public List<Node> getNodes() {
		return Collections.singletonList(new Node(nodeId, true));
	}
}
