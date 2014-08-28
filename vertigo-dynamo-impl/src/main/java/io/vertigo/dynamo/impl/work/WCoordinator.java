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
import io.vertigo.dynamo.impl.work.worker.distributed.DistributedWorker;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.io.Closeable;
import java.util.concurrent.Future;

/**
 * Coordinates the work performed by the workers.
 * 
 * @author pchretien, npiedeloup
 */
final class WCoordinator implements Closeable {
	private final WorkListener workListener;
	//There is always ONE LocalWorker, but distributedWorker is optionnal
	private final LocalWorker localWorker;
	private final Option<DistributedWorker> distributedWorker;

	WCoordinator(final int workerCount, final WorkListener workListener, final Option<MasterPlugin> masterPlugin) {
		Assertion.checkNotNull(workListener);
		Assertion.checkNotNull(masterPlugin);
		//-----------------------------------------------------------------
		localWorker = new LocalWorker(workerCount);
		this.workListener = workListener;
		distributedWorker = masterPlugin.isDefined() ? Option.some(new DistributedWorker(masterPlugin.get())) : Option.<DistributedWorker> none();
	}

	/** {@inheritDoc} */
	public void close() {
		localWorker.close();
	}

	/** {@inheritDoc}   */
	public <WR, W> Future<WR> submit(final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		final Worker worker = resolveWorker(workItem);
		//---
		workListener.onStart(workItem.getWorkType());
		boolean executed = false;
		final long start = System.currentTimeMillis();
		try {
			final Future<WR> future = worker.submit(workItem, workResultHandler);
			executed = true;
			return future;
		} finally {
			workListener.onFinish(workItem.getWorkType(), System.currentTimeMillis() - start, executed);
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
		if (distributedWorker.isDefined() && distributedWorker.get().accept(workItem)) {
			return distributedWorker.get();
		}
		return localWorker;
		//Gestion de la stratégie de distribution des works
		//		return distributedWorkerPlugin.isDefined() && distributedWorkerPlugin.get().getWorkFamily().equalsIgnoreCase(work.getWorkFamily());
	}
}
