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
package io.vertigo.dynamo.plugins.work.rest.worker;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.impl.node.NodePlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.node.Node;
import io.vertigo.dynamo.plugins.work.WResult;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.kernel.lang.Assertion;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

/**
 * Implémentation de DistributedWorkManager, pour l'execution de travaux par des Workers distant.
 * Cette implémentation représente la partie client qui se déploie en ferme.
 * 1- contacte la partie serveur pour récupérer les travaux qu'elle sait gérer,
 * 2- execute la tache en synchrone exclusivement
 * 3- retourne le résultat au serveur
 * 
 * @author npiedeloup, pchretien
 */
public final class RestNodePlugin  implements NodePlugin {
	private final List<String> workTypes;
	/** Log du workerNode. */
	private final Logger logger = Logger.getLogger(RestNodePlugin.class);
	private final WorkQueueRestClient workQueueClient; //devrait etre un plugin

	/**
	 * Constructeur.
	 * @param nodeId Identifiant du noeud
	 * @param workTypes Types de travail gérés
	 * @param serverUrl Url du serveur
	 * @param workManager Manager des works
	 * @param codecManager Manager d'encodage/decodage
	 */
	@Inject
	public RestNodePlugin(@Named("nodeId") final String nodeId, @Named("workTypes") final String workTypes, @Named("serverUrl") final String serverUrl, final WorkManager workManager, final CodecManager codecManager) {
		Assertion.checkArgNotEmpty(workTypes);
		Assertion.checkArgNotEmpty(serverUrl);
		//---------------------------------------------------------------------
		this.workTypes = Arrays.asList(workTypes.trim().split(";"));
		workQueueClient = new WorkQueueRestClient(nodeId, serverUrl + "/workQueue", codecManager);
	}

	public List<String> getWorkTypes() {
		return workTypes;
	}

	/** {@inheritDoc} */
	public List<Node> getNodes() {
		return null; //Collections.singletonList(new Node(getNodeId(), true));
	}

	@Override
	public <WR, W> WorkItem<WR, W> pollWorkItem(final String workType,final int timeoutInSeconds) {
		return workQueueClient.pollWorkItem(workType, timeoutInSeconds);
	}

	@Override
	public <WR> void putResult(final WResult<WR> result) {
		workQueueClient.putResult(result);
	}

	@Override
	public void putStart(final String workId) {
		workQueueClient.putStart(workId);
	}
}