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

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.plugins.work.WFuture;
import io.vertigo.dynamo.plugins.work.WResult;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Queue partagée sur une seule JVM.
 * La récupération des données est effective toutes les secondes.
 * 
 * @author pchretien
 */
final class RestQueue {
	//pas besoin de synchronized la map, car le obtain est le seul accès et est synchronized
	private final Map<String, BlockingQueue<WorkItem<?, ?>>> workQueueMap = new HashMap<>();

	//-------------A unifier avec RedisQueue
	private final Map<String, WorkResultHandler> workResultHandlers = Collections.synchronizedMap(new HashMap<String, WorkResultHandler>());

	<WR, W> Future<WR> submit(final String workType, final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		putWorkItem(workType, workItem);
		return createFuture(workItem.getId(), workResultHandler);
	}

	private <WR, W> Future<WR> createFuture(final String workId, final Option<WorkResultHandler<WR>> workResultHandler) {
		Assertion.checkNotNull(workId);
		//---------------------------------------------------------------------
		final WFuture<WR> future;
		if (workResultHandler.isDefined()) {
			future = new WFuture<>(workResultHandler.get());
		} else {
			future = new WFuture<>();
		}
		workResultHandlers.put(workId, future);
		return future;
	}

	void setResult(final WResult result) {
		final WorkResultHandler workResultHandler = workResultHandlers.remove(result.getWorkId());
		if (workResultHandler != null) {
			//Que faire sinon
			workResultHandler.onDone(result.hasSucceeded(), result.getResult(), result.getError());
		}
	}

	//-------------/A unifier avec RedisQueue

	/**
	 * Récupération du travail à effectuer.
	 * @return Prochain WorkItem ou null
	 */
	public WorkItem<?, ?> pollWorkItem(final String workType, final int timeoutInSeconds) {
		try {
			//take attend qu'un élément soit disponible toutes les secondes.
			//Poll attend (1s) qu'un élément soit disponible et sinon renvoit null
			final WorkItem<?, ?> workItem = obtainWorkQueue(workType).poll(timeoutInSeconds, TimeUnit.SECONDS);
			return workItem;
		} catch (final InterruptedException e) {
			//dans le cas d'une interruption on arrête de dépiler
			return null;
		}
	}

	/**
	 * Ajoute un travail à faire.
	 * @param <WR> Type du résultat
	 * @param <W> Travail à effectué
	 * @param workType Type du travail
	 * @param workItem Work et WorkResultHandler
	 */
	private <WR, W> void putWorkItem(final String workType, final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//-------------------------------------------------------------------
		try {
			obtainWorkQueue(workType).put(workItem);
		} catch (final InterruptedException e) {
			//dans le cas d'une interruption on interdit d'empiler de nouveaux Works
			throw new RuntimeException("putWorkItem", e);
		}
	}

	private synchronized BlockingQueue<WorkItem<?, ?>> obtainWorkQueue(final String workType) {
		BlockingQueue<WorkItem<?, ?>> workQueue = workQueueMap.get(workType);
		if (workQueue == null) {
			workQueue = new LinkedBlockingQueue<>();
			workQueueMap.put(workType, workQueue);
		}
		return workQueue;
	}
}
