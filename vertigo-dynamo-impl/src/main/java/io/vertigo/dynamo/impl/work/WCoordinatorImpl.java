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
import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

final class WCoordinatorImpl implements WCoordinator {
	private final WorkListener workListener;
	private final Option<DistributedWorkerPlugin> distributedWorker;
	private final LocalWorker localWorker;

	WCoordinatorImpl(final int workerCount, final WorkListener workListener, final Option<DistributedWorkerPlugin> distributedWorker) {
		Assertion.checkNotNull(workListener);
		Assertion.checkNotNull(distributedWorker);
		//-----------------------------------------------------------------
		localWorker = new LocalWorker(workerCount);
		this.workListener = workListener;
		this.distributedWorker = distributedWorker;
	}

	/** {@inheritDoc} */
	public void start() {
		//localWorker n'étant pas un plugin 
		//il faut le démarrer et l'arréter explicitement.
		localWorker.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		localWorker.stop();
	}

	/** {@inheritDoc}   */
	public <WR, W> void execute(final WorkItem<WR, W> workItem) {
		final Worker worker = resolveWorker(workItem);
		//---
		workListener.onStart(workItem.getWorkEngineProvider().getName());
		boolean executed = false;
		final long start = System.currentTimeMillis();
		try {
			worker.execute(workItem);
			executed = true;
		} finally {
			workListener.onFinish(workItem.getWorkEngineProvider().getName(), System.currentTimeMillis() - start, executed);
		}
	}

	private <WR, W> Worker resolveWorker(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//----------------------------------------------------------------------
		/* 
		 * On recherche un Worker capable d'effectuer le travail demandé.
		 * 1- On recherche parmi les works externes 
		 * 2- Si le travail n'est pas déclaré comme étant distribué on l'exécute localement
		 */
		if (distributedWorker.isDefined() && distributedWorker.get().canProcess(workItem.getWorkEngineProvider())) {
			return distributedWorker.get();
		}
		return localWorker;
		//Gestion de la stratégie de distribution des works
		//		return distributedWorkerPlugin.isDefined() && distributedWorkerPlugin.get().getWorkFamily().equalsIgnoreCase(work.getWorkFamily());
	}

}
