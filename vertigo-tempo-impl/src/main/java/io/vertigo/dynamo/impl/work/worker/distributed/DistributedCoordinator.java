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
package io.vertigo.dynamo.impl.work.worker.distributed;

import io.vertigo.dynamo.impl.work.MasterPlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.WorkResult;
import io.vertigo.dynamo.impl.work.worker.Coordinator;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author pchretien
 */
public final class DistributedCoordinator implements Coordinator, Activeable {
	private final MasterPlugin masterPlugin;
	private final Thread watcher;
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());

	public DistributedCoordinator(final MasterPlugin masterPlugin) {
		Assertion.checkNotNull(masterPlugin);
		//-----
		this.masterPlugin = masterPlugin;
		watcher = createWatcher();
	}

	/** {@inheritDoc} */
	@Override
	public <R, W> Future<R> submit(final WorkItem<R, W> workItem, final Option<WorkResultHandler<R>> workResultHandler) {
		//2. On attend les notifs sur un thread séparé, la main est rendue de suite
		final WFuture<R> future = createFuture(workItem.getId(), workResultHandler);
		putWorkItem(workItem, future);
		return future;
	}

	private static <R, W> WFuture<R> createFuture(final String workId, final Option<WorkResultHandler<R>> workResultHandler) {
		Assertion.checkNotNull(workId);
		//-----
		final WFuture<R> future;
		if (workResultHandler.isDefined()) {
			future = new WFuture<>(workResultHandler.get());
		} else {
			future = new WFuture<>();
		}
		return future;
	}

	/**
	 * Indique si ce type de work peut-être distribué.
	 * @param workItem Travail à effectuer
	 * @return si ce type de work peut-être distribué.
	 */
	public <R, W> boolean accept(final WorkItem<R, W> workItem) {
		return masterPlugin.acceptedWorkTypes().contains(workItem.getWorkType());
	}

	//-----

	private <R> void setResult(final String workId, final R result, final Throwable error) {
		Assertion.checkArgNotEmpty(workId);
		Assertion.checkArgument(result == null ^ error == null, "result xor error is null");
		//-----
		final WorkResultHandler workResultHandler = workResultHandlers.remove(workId);
		if (workResultHandler != null) {
			//Que faire sinon
			workResultHandler.onDone(result, error);
		}
	}

	private Thread createWatcher() {
		return new Thread("DistributedWorkResultWatcher") {
			/** {@inheritDoc} */
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					//On attend le résultat (par tranches de 1s)
					final int waitTimeSeconds = 1;
					final WorkResult result = masterPlugin.pollResult(waitTimeSeconds);
					if (result != null) {
						setResult(result.workId, result.result, result.error);
					}
				}
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		watcher.start();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		watcher.interrupt();
		try {
			watcher.join();
		} catch (final InterruptedException e) {
			//On ne fait rien
		}
	}

	private <R, W> void putWorkItem(final WorkItem<R, W> workItem, final WorkResultHandler<R> workResultHandler) {
		workResultHandlers.put(workItem.getId(), workResultHandler);
		masterPlugin.putWorkItem(workItem);
	}
}
