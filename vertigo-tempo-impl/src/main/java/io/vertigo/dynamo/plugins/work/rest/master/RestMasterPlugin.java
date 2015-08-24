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
import io.vertigo.dynamo.impl.work.MasterPlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.WorkResult;
import io.vertigo.lang.Assertion;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Exécution synchrone et distante des Works avec un transfert par WS REST.
 *
 * @author npiedeloup, pchretien
 */
public final class RestMasterPlugin implements MasterPlugin {
	private final RestQueueServer restQueueRestServer;
	private final List<String> distributedWorkTypes;

	/**
	 * Constructeur.
	 * @param daemonManager Manager des daemons
	 * @param timeoutSeconds Timeout des travaux en attente de traitement
	 * @param distributedWorkTypes Liste des types de work distribués (séparateur ;)
	 * @param codecManager Manager d'encodage/decodage
	 */
	@Inject
	public RestMasterPlugin(final DaemonManager daemonManager, final @Named("distributedWorkTypes") String distributedWorkTypes, @Named("timeoutSeconds") final int timeoutSeconds, final CodecManager codecManager) {
		Assertion.checkArgNotEmpty(distributedWorkTypes);
		Assertion.checkArgument(timeoutSeconds < 10000, "Le timeout s'exprime en seconde.");
		//-----
		this.distributedWorkTypes = Arrays.asList(distributedWorkTypes.split(";"));
		//	this.timeoutSeconds = timeoutSeconds;
		restQueueRestServer = new RestQueueServer(20, codecManager, 5);
		daemonManager.registerDaemon("workQueueTimeoutCheck", DeadNodeDetectorDaemon.class, 10);
	}

	/** {@inheritDoc} */
	@Override
	public List<String> acceptedWorkTypes() {
		return distributedWorkTypes;
	}

	/**
	 * @return RestQueueServer
	 */
	RestQueueServer getWorkQueueRestServer() {
		return restQueueRestServer;
	}

	/** {@inheritDoc} */
	@Override
	public WorkResult pollResult(final int waitTimeSeconds) {
		return getWorkQueueRestServer().pollResult(waitTimeSeconds);
	}

	/** {@inheritDoc} */
	@Override
	public <WR, W> void putWorkItem(final WorkItem<WR, W> workItem) {
		getWorkQueueRestServer().putWorkItem(workItem);
	}

	public static class DeadNodeDetectorDaemon implements Daemon {
		@Inject
		private MasterPlugin restMasterPlugin;

		/** {@inheritDoc} */
		@Override
		public void run() {
			((RestMasterPlugin) restMasterPlugin).getWorkQueueRestServer().checkDeadNodes();
			((RestMasterPlugin) restMasterPlugin).getWorkQueueRestServer().checkDeadWorkItems();
		}
	}
}
