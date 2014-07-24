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
package io.vertigo.dynamo.impl.work;

import io.vertigo.dynamo.impl.work.listener.WorkListener;
import io.vertigo.dynamo.impl.work.listener.WorkListenerImpl;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Option;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation de workManager.
 * 
 * @author pchretien, npiedeloup
 */
public final class WorkManagerImpl implements WorkManager, Activeable {
	private final WCoordinator coordinator;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager de la performance applicative
	 */
	@Inject
	public WorkManagerImpl(final @Named("workerCount") int workerCount, Option<DistributedWorkerPlugin> distributedWorker) {
		final WorkListener workListener = new WorkListenerImpl(/*analyticsManager*/);
		coordinator = new WCoordinatorImpl(workerCount, workListener, distributedWorker);
	}

	/** {@inheritDoc} */
	public void start() {
		//coordinator n'étant pas un plugin 
		//il faut le démarrer et l'arréter explicitement.
		coordinator.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		coordinator.stop();
	}

	/** {@inheritDoc} */
	public <WR, W> WR process(W work, WorkEngineProvider<WR, W> workEngineProvider) {
		WorkItem<WR, W> workItem = new WorkItem<>(work, workEngineProvider);
		coordinator.execute(workItem);
		return workItem.getResult();
	}

	public <WR, W> void schedule(W work, WorkEngineProvider<WR, W> workEngineProvider, WorkResultHandler<WR> workResultHandler) {
		WorkItem<WR, W> workItem = new WorkItem<>(work, workEngineProvider, workResultHandler);
		coordinator.execute(workItem);
	}

	public <WR, W> void schedule(Callable<WR> callable, WorkResultHandler<WR> workResultHandler) {
		WorkItem<WR, W> workItem = new WorkItem<>(callable, workResultHandler);
		coordinator.execute(workItem);
	}
}
