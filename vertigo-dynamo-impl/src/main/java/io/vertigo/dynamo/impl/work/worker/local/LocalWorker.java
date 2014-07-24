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
package io.vertigo.dynamo.impl.work.worker.local;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation d'un pool local de {@link Worker}.
 * 
 * @author pchretien
 */
public final class LocalWorker implements Worker, Activeable {
	/** paramètre du plugin définissant la taille de la queue. */
	private final WorkersPool workersPool;
	private boolean active;

	/**
	 * Constructeur.
	 * 
	 * @param workerCount paramètres d'initialisation du pool
	 */
	public LocalWorker(final int workerCount) {
		Assertion.checkArgument(workerCount >= 1, "At least one thread must be allowed to process asynchronous jobs.");
		// ---------------------------------------------------------------------
		workersPool = new WorkersPool(this, workerCount);
	}

	/** {@inheritDoc} */
	public void start() {
		workersPool.start();
		active = true;
	}

	/** {@inheritDoc} */
	public void stop() {
		active = false;
		workersPool.stop();
	}

	/** {@inheritDoc} */
	public <WR, W> void schedule(final WorkItem<WR, W> workItem) {
		Assertion.checkArgument(active, "plugin is not yet started");
		Assertion.checkNotNull(workItem);
		// ---------------------------------------------------------------------
		workersPool.putWorkItem(workItem);
	}

	/** {@inheritDoc} */
	public <WR, W> void process(final WorkItem<WR, W> workItem) {
		Assertion.checkArgument(active, "plugin is not yet started");
		Assertion.checkNotNull(workItem);
		// ---------------------------------------------------------------------
		workItem.setResult(workItem.getWorkEngineProvider().provide().process(workItem.getWork()));
	}
}
