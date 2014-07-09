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
import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation de workManager.
 * 
 * @author pchretien, npiedeloup
 */
public final class WorkManagerImpl implements WorkManager, Activeable {

	private final LocalWorker localWorker;

	@Inject
	private Option<DistributedWorkerPlugin> distributedWorker;
	private final WorkListener workListener;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager de la performance applicative
	 */
	@Inject
	public WorkManagerImpl(final @Named("workerCount") int workerCount/*, final AnalyticsManager analyticsManager*/) {
		workListener = new WorkListenerImpl(/*analyticsManager*/);
		localWorker = new LocalWorker(workerCount);
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
	public <WR, W> void process(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//----------------------------------------------------------------------
		//On délégue l'exécution synchrone et locale à un Worker.
		workListener.onStart(workItem.getWorkEngineProvider().getName());
		boolean executed = false;
		final long start = System.currentTimeMillis();
		try {
			resolveWorker(workItem).process(workItem);
			executed = true;
		} finally {
			workListener.onFinish(workItem.getWorkEngineProvider().getName(), System.currentTimeMillis() - start, executed);
		}
	}

	private <WR, W> Worker resolveWorker(final WorkItem<WR, W> workItem) {
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

	/** {@inheritDoc} */
	public <WR, W> void schedule(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//----------------------------------------------------------------------
		resolveWorker(workItem).schedule(workItem);
	}
}
