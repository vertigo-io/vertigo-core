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
package io.vertigo.dynamo.plugins.work.rest;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.kernel.lang.Assertion;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Queue partag�e sur une seule JVM.
 * La r�cup�ration des donn�es est effective toutes les secondes.
 * 
 * @author pchretien
 * @version $Id: MultipleWorkQueues.java,v 1.10 2014/02/27 10:31:19 pchretien Exp $
 */
final class MultipleWorkQueues {
	//pas besoin de synchronized la map, car le obtain est le seul acc�s et est synchronized
	private final Map<String, BlockingQueue<WorkItem<?, ?>>> workQueueMap = new HashMap<>();

	/**
	 * R�cup�ration du travail � effectuer.
	 * @return Prochain WorkItem ou null
	 */
	public WorkItem<?, ?> pollWorkItem(final String workType) {
		try {
			//take attend qu'un �l�ment soit disponible toutes les secondes.
			//Poll attend (1s) qu'un �l�ment soit disponible et sinon renvoit null
			final WorkItem<?, ?> workItem = obtainWorkQueue(workType, workQueueMap).poll(1, TimeUnit.SECONDS);
			return workItem;
		} catch (final InterruptedException e) {
			//dans le cas d'une interruption on arr�te de d�piler 
			return null;
		}
	}

	/**
	 * Ajoute un travail � faire.
	 * @param <WR> Type du r�sultat
	 * @param <W> Travail � effectu�
	 * @param workType Type du travail
	 * @param workItem Work et WorkResultHandler
	 */
	public <WR, W> void putWorkItem(final String workType, final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//-------------------------------------------------------------------
		try {
			obtainWorkQueue(workType, workQueueMap).put(workItem);
		} catch (final InterruptedException e) {
			//dans le cas d'une interruption on interdit d'empiler de nouveaux Works 
			throw new RuntimeException("putWorkItem", e);
		}
	}

	private synchronized BlockingQueue<WorkItem<?, ?>> obtainWorkQueue(final String workType, final Map<String, BlockingQueue<WorkItem<?, ?>>> queueMap) {
		BlockingQueue<WorkItem<?, ?>> workQueue = queueMap.get(workType);
		if (workQueue == null) {
			workQueue = new LinkedBlockingQueue<>();
			queueMap.put(workType, workQueue);
		}
		return workQueue;
	}

}
