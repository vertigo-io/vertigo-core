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
package io.vertigo.dynamo.impl.node;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.local.LocalCoordinator;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

final class WWorker implements Runnable {
	private static final int TIMEOUT_IN_SECONDS = 1;
	private final LocalCoordinator localWorker;
	private final String workType;
	private final WorkerPlugin workerPlugin;

	WWorker(/*final String nodeId,*/final String workType, final LocalCoordinator localWorker, final WorkerPlugin nodePlugin) {
		//Assertion.checkArgNotEmpty(nodeId);
		Assertion.checkArgNotEmpty(workType);
		Assertion.checkNotNull(localWorker);
		Assertion.checkNotNull(nodePlugin);
		//-----------------------------------------------------------------
		//	this.nodeId = nodeId;
		this.workType = workType;
		this.localWorker = localWorker;
		this.workerPlugin = nodePlugin;
	}

	/** {@inheritDoc} */
	@Override
	public final void run() {
		while (!Thread.interrupted()) {
			doRun();
		}
	}

	private <WR, W> void doRun() {
		final WorkItem<WR, W> workItem = workerPlugin.<WR, W> pollWorkItem(workType, TIMEOUT_IN_SECONDS);
		if (workItem != null) {

			final Option<WorkResultHandler<WR>> workResultHandler = Option.<WorkResultHandler<WR>> some(new WorkResultHandler<WR>() {
				public void onStart() {
					workerPlugin.putStart(workItem.getId());
				}

				public void onDone(final WR result, final Throwable error) {
					workerPlugin.putResult(workItem.getId(), result, error);
				}
			});
			//---Et on fait executer par le workerLocalredisDB
			localWorker.submit(workItem, workResultHandler);
		}
		//if workitem is null, that's mean there is no workitem available;
	}

}
