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
package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.plugins.work.WResult;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

/**
 * @author pchretien
 * $Id: RedisDispatcherThread.java,v 1.8 2014/02/03 17:28:45 pchretien Exp $
 */
final class RedisDispatcherThread extends Thread {
	private final RedisDB redisDB;
	//	private final String nodeId;
	private final LocalWorker localWorker;
	private final String workType;

	RedisDispatcherThread(final String nodeId, final String workType, final RedisDB redisDB, final LocalWorker localWorker) {
		Assertion.checkArgNotEmpty(nodeId);
		Assertion.checkArgNotEmpty(workType);
		Assertion.checkNotNull(redisDB);
		Assertion.checkNotNull(localWorker);
		//-----------------------------------------------------------------
		this.redisDB = redisDB;
		//	this.nodeId = nodeId;
		this.workType = workType;
		this.localWorker = localWorker;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		while (!isInterrupted()) {
			doRun();
		}
	}

	private static final int TIMEOUT_IN_SECONDS = 1;

	private <WR, W> void doRun() {
		final WorkItem<WR, W> workItem = redisDB.pollWorkItem(workType, TIMEOUT_IN_SECONDS);
		if (workItem != null) {

			final Option<WorkResultHandler<WR>> workResultHandler = Option.<WorkResultHandler<WR>> some(new WorkResultHandler<WR>(){
				public void onStart() {
					// TODO Auto-generated method stub
				}

				public void onDone(final boolean succeeded, final WR result, final Throwable error) {
					redisDB.putResult(new WResult(workItem.getId(), succeeded, result, error));
				}
			});
			//---Et on fait executer par le workerLocalredisDB
			localWorker.submit(workItem, workResultHandler);
		}
		//if workitem is null, that's mean there is no workitem available;
	}

}
