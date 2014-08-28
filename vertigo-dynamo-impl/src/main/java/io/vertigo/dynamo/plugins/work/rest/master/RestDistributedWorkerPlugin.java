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
import io.vertigo.dynamo.impl.work.DistributedWorkerPlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.plugins.work.master.WQueue;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Exécution synchrone et distante des Works avec un transfert par WS REST.
 * 
 * @author npiedeloup, pchretien
 */
public final class RestDistributedWorkerPlugin implements DistributedWorkerPlugin {
	//	private final long timeoutSeconds;
	private final Set<String> workTypes;
	private final WQueue queue;
	private final WorkQueueRestServer workQueueRestServer;

	/**
	 * Constructeur.
	 * @param timeoutSeconds Timeout des travaux en attente de traitement
	 * @param workTypesAsString Liste des types de work distribués (séparateur ;)
	 * @param codecManager Manager d'encodage/decodage
	 */
	@Inject
	public RestDistributedWorkerPlugin(@Named("timeoutSeconds") final long timeoutSeconds, @Named("workTypes") final String workTypesAsString, final CodecManager codecManager) {
		Assertion.checkArgument(timeoutSeconds < 10000, "Le timeout s'exprime en seconde.");
		Assertion.checkArgNotEmpty(workTypesAsString);
		//---------------------------------------------------------------------
		//	this.timeoutSeconds = timeoutSeconds;
		final String[] workTypesArray = workTypesAsString.split(";");//
		workTypes = new HashSet<>(Arrays.asList(workTypesArray));
		queue = new RestQueue();
		workQueueRestServer = new WorkQueueRestServer(queue, 20 * 1000, codecManager);
	}

	/**
	 * @return Serveur REST de la WorkQueue. (appelée par l'implé Jersey)
	 */
	public WorkQueueRestServer getWorkQueueRestServer() {
		return workQueueRestServer;
	}

	//	/** {@inheritDoc} */
	//	public void start() {
	//		workQueueRestServer.start();
	//	}
	//
	//	/** {@inheritDoc} */
	//	public void stop() {
	//		workQueueRestServer.stop();
	//	}

	/** {@inheritDoc} */
	public <WR, W> boolean canProcess(final WorkEngineProvider<WR, W> workEngineProvider) {
		return workTypes.contains(obtainWorkType(workEngineProvider));
	}

	/** {@inheritDoc} */
	public <WR, W> Future<WR> submit(final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		final String workType = obtainWorkType(workItem);
		return queue.submit(workType, workItem, workResultHandler);
	}


	private <WR, W> String obtainWorkType(final WorkEngineProvider workEngineProvider) {
		return workEngineProvider.getName();
	}

	private <WR, W> String obtainWorkType(final WorkItem<WR, W> workitem) {
		return obtainWorkType(workitem.getWorkEngineProvider());
	}
}
